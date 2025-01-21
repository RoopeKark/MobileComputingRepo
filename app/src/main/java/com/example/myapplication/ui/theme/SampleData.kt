package com.example.myapplication.ui.theme
import com.example.myapplication.Message
import com.example.myapplication.R

object SampleData {
    private val picture1 = R.drawable.dobby_512x512_
    private val picture2 = R.drawable.harrypotter_512x512_
    private const val USER1 = "Dobby"
    private const val USER2 = "Harry Potter"
    // Sample conversation data
    val conversationSample = listOf(
        Message(
            USER1,
            "Hello Mister Harry Potter",
            picture1
        ),
        Message(
            USER2,
            "Hey Dobby, what you up to",
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
            "Good! But I must leave now Mister Potter",
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
}