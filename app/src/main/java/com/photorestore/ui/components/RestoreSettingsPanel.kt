package com.photorestore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.photorestore.domain.model.RestorationSettings

@Composable
fun RestoreSettingsPanel(
    onRestore: (RestorationSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var sharpen by remember { mutableFloatStateOf(0f) }
    var denoise by remember { mutableStateOf(true) }
    var colorize by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Custom Settings", style = MaterialTheme.typography.titleSmall)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Brightness
        SettingSlider("Brightness", brightness, -0.5f, 0.5f) { brightness = it }
        
        // Contrast
        SettingSlider("Contrast", contrast, 0.5f, 2f) { contrast = it }
        
        // Saturation
        SettingSlider("Saturation", saturation, 0f, 2f) { saturation = it }
        
        // Sharpen
        SettingSlider("Sharpen", sharpen, 0f, 1f) { sharpen = it }
        
        // Switches
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Denoise")
            Switch(checked = denoise, onCheckedChange = { denoise = it })
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto Colorize")
            Switch(checked = colorize, onCheckedChange = { colorize = it })
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = {
                onRestore(RestorationSettings(
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    sharpness = sharpen,
                    denoise = denoise,
                    colorize = colorize
                ))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Custom Restore")
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("%.2f".format(value), style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
