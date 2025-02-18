package com.example.mobilecomputingproject

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.mobilecomputingproject.ui.theme.MobileComputingProjectTheme
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.mobilecomputingproject.ui.theme.getSampleData
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import androidx.core.content.ContextCompat

private var userName: String = "User"
//private var imageUri: Uri? = null

object GlobalState {
    var profile_picture = mutableStateOf<Uri?>(null)
    var sensor_value = mutableFloatStateOf(0f)
}

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialise DataBase
        val db: AppDatabase = AppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch {
            val userDao = db.userDao()
            val tempName = userDao.getUser()?.userName
            if (tempName != null){
                userName = tempName
            }
            GlobalState.profile_picture.value = loadSavedPFP(applicationContext)
            Log.d("PhotoLoader", "photo uri: ${GlobalState.profile_picture.value}")
        }
        //Initialise Sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //Start sensor listener
        startSensorListener()
        //Initialise Notification
        NotificationSender(this).createNotificationChannel()

        //set Main Content
        setContent {
            MobileComputingProjectTheme {
                Surface {
                    AppStart(db = db)
                }
            }
        }
    }

    //Sensor Stuff
    private fun startSensorListener() {
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Log.e("SensorError", "Light sensor not available on this device")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            GlobalState.sensor_value.floatValue = event.values[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this) // Prevent memory leaks
    }
    //end Sensor stuff
}



data class Message(val author: String, val body: String, val picture: Any)

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
                onNavigateToWelcome = { navController.navigate(route = MainScreen) }
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
fun NotificationButton(notificationId: Int, title: String, message: String, update: Boolean = false, buttonText: String = "Send Notification") {
    //val context = LocalContext.current as Activity
    val context = LocalActivity.current as Activity
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    Button(onClick = {
        if (update) {
            NotificationSender(context)
                .updateNotification(
                    notificationId = notificationId,
                    title = title,
                    message = message,
                    intent = pendingIntent
                )
        } else {
            NotificationSender(context)
                .sendNotification(
                    notificationId = notificationId,
                    title = title,
                    message = message,
                    intent = pendingIntent
                )
        }
    }) {
        Text(buttonText)
    }
}

@Composable
fun CancelNotificationButton(notificationId: Int) {
    val context = LocalActivity.current as Activity
    Button(onClick = {
        NotificationSender(context)
            .cancelNotification(notificationId = notificationId)
    }) {
        Text("Cancel Notification")
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
){
    val updatedMessage: String = "Is way too bright now: ${GlobalState.sensor_value.floatValue.toString()} luxes!"
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
        NotificationButton(title = "Hello", message = "World", notificationId = 1)
        if (GlobalState.sensor_value.floatValue > 1000) {
            NotificationSender(LocalActivity.current as Activity)
                .updateNotification(1,"It got bright", updatedMessage, null)
        }
        CancelNotificationButton(notificationId = 1)
        EnableNotifications()
    }
}


@Composable
fun EnableNotifications() {
    val context = LocalContext.current
    var requestPermission by remember { mutableStateOf(false) }

    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    if (!isPermissionGranted){
        Button (
            onClick = { requestPermission = true }
        ) {
            Text("Enable Notifications")
        }

    }
    if (requestPermission) {
        RequestNotificationPermissions(
            onPermissionGranted = {
                isPermissionGranted = true
            }
        )
    }


}

@Composable
fun ProfileScreen(onBackButton: () -> Unit, db: AppDatabase) {

    val context = LocalContext.current
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope() // Remember a coroutine scope

    var localImageUri by remember { mutableStateOf(GlobalState.profile_picture.value) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

        if (uri != null) {
            localImageUri = uri
            Log.d("PhotoPicker", "Selected mediaURI!: $uri")
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


    var newUserName by rememberSaveable { mutableStateOf(userName) }

    LaunchedEffect(Unit) {
        val fetchedName = withContext(Dispatchers.IO) {
            userDao.getUser()?.userName ?: userName
        }
        newUserName = fetchedName
    }

    val newUser = User(userName = newUserName, profilePicture = R.drawable.harrypotter_512x512_)



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
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(localImageUri)
                        .crossfade(false)
                        .build(),
                    loading = {
                        CircularProgressIndicator()
                    },
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(onClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }),
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
                    value = newUserName,
                    onValueChange = { newUserName = it },
                    label = { Text("Username") },
                )
            }
            Button( onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    db.userDao().deleteAllUsers()
                    userName = newUser.userName
                    db.userDao().insert(newUser)

                    GlobalState.profile_picture.value = localImageUri

                    GlobalState.profile_picture.value?.let {
                        savePFP(context, it)
                    }
                }
            }) {
                Text(text = "Save")
            }
            Text(text = "LightSensor: ${GlobalState.sensor_value.floatValue}")
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


fun savePFP(context: Context, uri: Uri, fileName: String = "profile_picture.jpg") {

    val resolver = context.contentResolver
    val file = File(context.filesDir, fileName)

    resolver.openInputStream(uri)?.use { stream ->
        context.openFileOutput(file.name, Context.MODE_PRIVATE).use { output ->
            stream.copyTo(output)
        }
    }
    Log.d("ProfilePicture", "Image saved to: ${file.absolutePath}")
}


fun loadSavedPFP(context: Context, fileName: String = "profile_picture.jpg"): Uri? {
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        val uri = Uri.fromFile(file)
        Log.d("PhotoLoader", "found: $uri")
        return uri
    } else {
        Log.d("PhotoLoader", "uri not found")
        return null
    }
}

@Composable
@Preview
fun ProfileScreenPreview(){
    val context = LocalContext.current
    val db = remember {
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Only for testing! Don't use in production.
            .build()
    }
    MobileComputingProjectTheme {
        Surface {
            ProfileScreen({}, db = db)
        }
    }
}

@Composable
fun SecondScreen(onBackButton: () -> Unit, onProfileButton: () -> Unit){
    val localImageUri: Any = if (GlobalState.profile_picture.value != null) {
        GlobalState.profile_picture.value as Uri
    } else {
        AsyncImagePainter.State.Empty
    }
    TopHeaderAndContent(
        getDefaultHeaderContent(onBackButton = onBackButton, onProfileButton = onProfileButton),
        listOf { Conversation(getSampleData(userName, localImageUri)) })
}


fun getDefaultHeaderContent(
    buttonText: String = "Back",
    midText: String = "Hello",
    onBackButton: () -> Unit,
    onProfileButton: () -> Unit,
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
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(GlobalState.profile_picture.value)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
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
    MobileComputingProjectTheme {
        //WelcomeScreen(onNavigateToWelcome = {})
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
    MobileComputingProjectTheme {
        Surface {
            TopHeaderAndContent(
                getDefaultHeaderContent(onBackButton = {}, onProfileButton = {}),
                listOf { Conversation(getSampleData(userName, R.drawable.dobby_512x512_)) })
        }
    }
}

@Composable
fun MessageCard(msg: Message) {
    // Add padding around our message
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Log.d("msg.picture", "msg.picture: ${msg.picture}")
        when (msg.picture) {
            is Uri ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(GlobalState.profile_picture.value)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            else ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(msg.picture)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
        }

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
    MobileComputingProjectTheme {
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
            message.author
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
    MobileComputingProjectTheme {
        Surface {
            SecondScreen( onBackButton = {}, {} )
        }
    }
}