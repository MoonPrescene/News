package com.example.funiture_shop.repository

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.example.news.common.Const
import com.example.funiture_shop.data.model.res.Res
import com.example.funiture_shop.helper.SharedPreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

@Singleton
class LoginRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val db: FirebaseFirestore,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {

    private val loginLiveData = MutableLiveData<Res>()
    private val signUpLiveData = MutableLiveData<Res>()
    private val updateImageLiveData = MutableLiveData<Res>()
    private val upImageUserLiveData = MutableLiveData<Res>()
    private val resetPasswordLiveData = MutableLiveData<Res>()

    fun signIn(email: String, pass: String): MutableLiveData<Res> {
        //this signInWithEmailAndPassword run default on main thread :v
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                loginLiveData.postValue(Res.Success(data = null))
            } else {
                loginLiveData.postValue(Res.Error(it.exception.toString()))
            }
        }
        return loginLiveData
    }

    fun signUp(email: String, pass: String): MutableLiveData<Res> {
        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                signUpLiveData.postValue(Res.Success(data = null))
            } else {
                signUpLiveData.postValue(Res.Error(it.exception.toString()))
            }
        }
        return signUpLiveData
    }

    fun addImageToFirebaseStorage(image: Uri) {
        val storageRef = firebaseStorage.reference.child(Const.IMAGES)
            .child(sharedPreferencesHelper.getUserName())
        storageRef.putFile(image).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        updateImageLiveData.postValue(Res.Success(it))
                    } else {
                        updateImageLiveData.postValue(Res.Error(task.exception.toString()))
                    }
                }
            } else {
                updateImageLiveData.postValue(Res.Error(task.exception.toString()))
            }
        }
    }

    fun addImageUrlToFireStore(image: Uri) {
        val collectionRef = db.collection("users")
        collectionRef.document(sharedPreferencesHelper.getUserName())
            .update("imgUrl", image.toString()).addOnCompleteListener {
                if (it.isSuccessful) {
                    upImageUserLiveData.postValue(Res.Success(data = null))
                } else {
                    upImageUserLiveData.postValue(Res.Error(it.exception.toString()))
                }
            }

    }

    fun resetPassword() {
        firebaseAuth.sendPasswordResetEmail(sharedPreferencesHelper.getUserName())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    resetPasswordLiveData.postValue(Res.Success(data = null))
                } else {
                    resetPasswordLiveData.postValue(Res.Error(task.exception.toString()))
                }
            }
    }
}