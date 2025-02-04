package com.example.myapplication.ui.theme
import android.net.Uri
import com.example.myapplication.Message
import com.example.myapplication.R

fun getSampleData(userName: String, pfp: Any): List<Message> {
    val picture1: Int = R.drawable.dobby_512x512_
    val USER1: String = "Dobby"
    var picture2: Any = 0
    when (pfp) {
        is Int ->
            picture2 = if(pfp == 0) {R.drawable.harrypotter_512x512_} else { pfp }
        is Uri ->
            picture2 = if(pfp == null) {R.drawable.harrypotter_512x512_} else { pfp }
    }

    val USER2 = userName

    val conversationSample = listOf(
        Message(
            USER1,
            "Hello Mister $USER2",
            picture1
        ),
        Message(
            USER2,
            "Hey ${USER1}, what you up to",
            picture2
        ),
        Message(
            USER1,
            """I think Kotlin is my favorite programming language.
                |It's so much fun!""".trimMargin().trim(),
            picture1
        ),
        Message(
            USER2,
            "I see where did you come to that conclusion for?",
            picture2
        ),
        Message(
            USER1,
            """Hey, take a look at Jetpack Compose, it's great!
                |It's the Android's modern toolkit for building native UI.
                |It simplifies and accelerates UI development on Android.
                |Less code, powerful tools, and intuitive Kotlin APIs :)""".trimMargin().trim(),
            picture1
        ),
        Message(
            USER2,
            "I see i prefer magic tough",
            picture2
        ),
        Message(
            USER1,
            "Bu, but Mister Potter you must try this",
            picture1
        ),
        Message(
            USER2,
            "Why would I, I have no interest in developing",
            picture2
        ),
        Message(
            USER1,
            "But imagine what could you do with magic and programming!",
            picture1
        ),
        Message(
            USER2,
            "I see maybe I will give it a try",
            picture2
        ),
        Message(
            USER1,
            "Good! But I must leave now $USER2",
            picture1
        ),
        Message(
            USER2,
            "Bye bye",
            picture2
        ),
        Message(
            USER1,
            "Bye!",
            picture1
        ),
    )
    return conversationSample
}