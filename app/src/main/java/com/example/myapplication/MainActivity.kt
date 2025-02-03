package com.example.myapplication

import android.app.Application
import android.content.res.Configuration
import android.graphics.Paint
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.myapplication.ui.theme.SampleData
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "AppDatabad"
        ).build()

        val user1 = User(userName = "John Doe", profilePicture = 23)
        val user = db.userDao().findByName("John Doe")
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface {

                    AppStart(db = db)
                }
            }
        }
    }
}


data class Message(val author: String, val body: String, val picture: Int)
private val Username: String = "User"



@Serializable
object WelcomeScreen
@Serializable
object MainScreen
@Serializable
object ProfileScreen

@Composable
fun AppStart (
    db: AppDatabase,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = WelcomeScreen
    ) {
        composable<WelcomeScreen> {
            WelcomeScreen(
                onNavigateToWelcome = { navController.navigate(route = MainScreen) },
            )
        }
        composable<MainScreen> {
            SecondScreen(
                onBackButton = { navController.navigate(route = WelcomeScreen)
                {
                    popUpTo(route = WelcomeScreen) {
                        inclusive = true
                    }
                }
                },
                onProfileButton = { navController.navigate(route = ProfileScreen) }
            )
        }
        composable<ProfileScreen> {
            ProfileScreen(
                db = db,
                onBackButton = { navController.navigate(route = MainScreen)
                {
                    popUpTo(route = MainScreen) {
                        inclusive = true
                    }
                }
                },
            )
        }

    }
}

@Composable
fun WelcomeScreen(
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
    ){

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome!")
        Button(
            modifier = Modifier
                .padding(vertical = 24.dp),
            onClick = onNavigateToWelcome
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun ProfileScreen(onBackButton: () -> Unit, db: AppDatabase) {




    val content: List<@Composable () -> Unit> = listOf(
        {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    //.background(MaterialTheme.colorScheme.primary)
                    .padding(top = 30.dp),
                contentAlignment = Alignment.Center
            )
            {
                Image(
                    painter = painterResource(R.drawable.dobby_512x512_),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        // Set image size to 40 dp
                        .size(100.dp)
                        // Clip image to be shaped as a circle
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(onClick = { }),
                )
            }
        },
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextField(
                    value = "test",
                    onValueChange = {

                                    },
                    label = { Text("Username") },
                )
            }
            Button( onClick = {

            }) {
                Text(text = "Save")
            }
        }
    )
    TopHeaderAndContent(
        getDefaultHeaderContent(
            midText = "Profile",
            onBackButton = onBackButton,
            onProfileButton = {}
        ),
        content
    )
}

@Composable
@Preview
fun ProfileScreenPreview(){
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember {
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Only for testing! Don't use in production.
            .build()
    }
    MyApplicationTheme {
        Surface {
            ProfileScreen({}, db = db)
        }
    }
}

@Composable
fun SecondScreen(onBackButton: () -> Unit, onProfileButton: () -> Unit){

    TopHeaderAndContent(
        getDefaultHeaderContent(onBackButton = onBackButton, onProfileButton = onProfileButton),
        listOf({Conversation(SampleData.conversationSample)}))
}


fun getDefaultHeaderContent(
    buttonText: String = "Back",
    midText: String = "Hello",
    onBackButton: () -> Unit,
    onProfileButton: () -> Unit,
    image: Int = R.drawable.dobby_512x512_
): List<@Composable () -> Unit> {
    return listOf(
        {
            Button(onClick = onBackButton) {
                Text(buttonText)
            }
        },
        {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                text = midText,
                style = MaterialTheme.typography.titleLarge,
                //color = MaterialTheme.colorScheme.onPrimary
            )
        },
        {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 5.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(image),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        // Set image size to 40 dp
                        .size(50.dp)
                        // Clip image to be shaped as a circle
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(onClick = onProfileButton),
                )
            }
        }
    )
}


@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun WelcomeScreenPreview() {
    MyApplicationTheme {
        WelcomeScreen(onNavigateToWelcome = {})
    }
}

@Composable
fun TopHeaderAndContent(
    headerContent: List<@Composable () -> Unit>,
    bottomContent: List<@Composable () -> Unit>,
    )
{
    Column(modifier = Modifier
        .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(start = 2.dp, bottom = 2.dp, top = 20.dp, end = 2.dp),


        ) {
            headerContent.forEach {
                item -> item()
            }
        }
        bottomContent.forEach {
            item -> item()
        }
    }
}

@Preview
@Composable
fun PreviewTopHeader(){
    MyApplicationTheme {
        Surface {
            TopHeaderAndContent(getDefaultHeaderContent(onBackButton = {}, onProfileButton = {}), listOf({Conversation(SampleData.conversationSample)}))
        }
    }
}

@Composable
fun MessageCard(msg: Message) {
    // Add padding around our message
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(msg.picture),
            contentDescription = null,
            modifier = Modifier
                // Set image size to 40 dp
                .size(40.dp)
                // Clip image to be shaped as a circle
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        // Add a horizontal space between the image and the column
        Spacer(modifier = Modifier.width(8.dp))

        // We keep track if the message is expanded or not in this variable
        var isExpanded by remember { mutableStateOf(false) }
        //surfaceColor will be updated gradually from one color to another
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )
        // We toggle the isExpanded variable when we click on this Column
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text (
                text = msg.author,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                // surfaceColor will be changing gradually from primary to surface
                color = surfaceColor,
                // animateContentSize will change the surface size gradually
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // if the message is expanded, we display all its content
                    // otherwise we only display the first line
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }
    }
}

@Preview( name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    MyApplicationTheme {
        Surface {
            MessageCard(
                msg = Message("Lexi", "Hey, take a look at Jetpack Compose, it's great!", R.drawable.dobby_512x512_)
            )
        }
    }
}

@Composable
fun Conversation(messages: List<Message>){
    LazyColumn {
        items(messages) {message ->
            MessageCard(message)
        }
    }
}

@Preview ( name = "Light mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewSecondScreen() {
    MyApplicationTheme {
        Surface {
            SecondScreen( onBackButton = {}, {} )
        }
    }
}