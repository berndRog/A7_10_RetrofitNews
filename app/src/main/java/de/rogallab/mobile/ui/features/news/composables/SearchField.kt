package de.rogallab.mobile.ui.features.news.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.R

@Composable
fun SearchField(
   searchText: String,
   onSearchTextChange: (String) -> Unit,
   onTriggerSearch: () -> Unit
) {
   val keyboardController = LocalSoftwareKeyboardController.current
   OutlinedTextField(
      modifier = Modifier
         .padding(horizontal = 8.dp)
         .padding(bottom = 8.dp)
         .fillMaxWidth(),
      value = searchText,
      onValueChange = { onSearchTextChange(it) },
      label = {
         Text(text = stringResource(R.string.searchtext))
      },
      leadingIcon = {
         Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search News")
      },
      keyboardOptions = KeyboardOptions(
         keyboardType = KeyboardType.Text,
         imeAction = ImeAction.Search
      ),
      keyboardActions = KeyboardActions(
         onSearch = {
            onTriggerSearch()
            keyboardController?.hide()
         }
      ),
      textStyle = MaterialTheme.typography.titleMedium,
      singleLine = true,
   )
}