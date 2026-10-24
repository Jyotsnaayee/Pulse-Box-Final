package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreTaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserTasksCollection() = 
        auth.currentUser?.let { user ->
            firestore.collection("users").document(user.uid).collection("tasks")
        }

    val allTasks: Flow<List<Task>> = getUserTasksCollection()?.let { collection ->
        collection.orderBy("priority", Query.Direction.ASCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Task::class.java)
            }
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insert(task: Task) {
        val collection = getUserTasksCollection() ?: return
        val docRef = collection.document()
        val taskWithId = task.copy(firestoreId = docRef.id)
        docRef.set(taskWithId).await()
    }

    suspend fun update(task: Task) {
        val collection = getUserTasksCollection() ?: return
        val firestoreId = task.firestoreId ?: return
        collection.document(firestoreId).set(task).await()
    }

    suspend fun delete(task: Task) {
        val collection = getUserTasksCollection() ?: return
        val firestoreId = task.firestoreId ?: return
        collection.document(firestoreId).delete().await()
    }
}
