package de.rogallab.mobile.ui.news.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.domain.utilities.formatShortDate
import de.rogallab.mobile.domain.utilities.formatTimeMin
import de.rogallab.mobile.domain.utilities.toZonedDateTime

@Composable
fun NewsItem(
   article: Article,
   onClick: () -> Unit
) {

   Column(modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxWidth()
      .clickable { onClick() }
   ) {

      var text = article.source?.name ?: ""
      article.publishedAt.let {
         val zdt = toZonedDateTime(it)
         val date = "${zdt.format(formatShortDate)}"
         val time = "${zdt.format(formatTimeMin)}"
         text = "${article.source?.name}, $date, $time"
      }
      Text(
         text = text,
         style = MaterialTheme.typography.bodySmall
      )

      Text(
         text = article.title,
         style = MaterialTheme.typography.bodyLarge
      )

      article.urlToImage?.let { path: String ->
         AsyncImage(
            model = path,
            contentDescription = "Bild",
            modifier = Modifier
               .height(130.dp)
               .clip(RoundedCornerShape(percent = 5)),
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop
         )
      }

      Text(
         text = article.description ?: "",
         style = MaterialTheme.typography.bodyMedium
      )
      HorizontalDivider(modifier = Modifier
         .padding(vertical = 8.dp)
         .fillMaxWidth(),
         thickness = 4.dp
      )


   }
}