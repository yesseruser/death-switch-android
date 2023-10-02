package com.yesser_studios.death_switch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yesser_studios.death_switch.ui.theme.DeathSwitchTheme
import com.yesser_studios.death_switch.ui.theme.Typography
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val dataStore = preferenceDataStore(context = context)
    val scope = rememberCoroutineScope()
    val loadedDeathCount = dataStore.getDeaths().collectAsState(initial = 0)


    var checked by remember { mutableStateOf(false) }

    DeathSwitchTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)) {
                    Text("Death", modifier = Modifier.padding(4.dp),
                        style = Typography.bodyLarge)
                    Switch(
                        checked = checked,
                        modifier = Modifier.padding(4.dp),
                        onCheckedChange = {
                            checked = it
                            if (checked) {
                                scope.launch {
                                    dataStore.setDeaths(loadedDeathCount.value!! + 1)
                                }
                            }
                        },
                        thumbContent = {
                            if (checked) {
                                Icon(imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize))
                            }
                        })
                }
                if (checked){
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Text("You died!",
                            modifier = Modifier.padding(4.dp),
                            style = Typography.bodyLarge)
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                checked = false
                            }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Respawn",
                                modifier = Modifier.padding(4.dp))
                            Text(text = "Respawn", style = Typography.bodyLarge)
                        }
                    }
                }
            }

            DeathCounter(
                deaths = loadedDeathCount.value!!,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Bottom)
        }
    }
}

@Composable
fun DeathCounter (deaths: Int,
                  horizontalAlignment: Alignment.Horizontal,
                  verticalAlignment: Alignment.Vertical,
                  horizontalArrangement: Arrangement.Horizontal,
                  verticalArrangement: Arrangement.Vertical) {
    Column (
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement) {
            Row (
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment) {
                    Text(
                        text = "Number of deaths:",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(4.dp))
                    HighlightText(text = "$deaths", Modifier.padding(4.dp))
            }
    }
}

@Composable
fun HighlightText(text: String, modifier: Modifier) {
    Button( onClick = { }, modifier = modifier ) {
        Text(text = text, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DeathSwitchTheme {
        AppContent()
    }
}