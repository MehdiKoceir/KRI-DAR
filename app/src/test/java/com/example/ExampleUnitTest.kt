package com.example

import com.example.data.model.PropertyReview
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun propertyReview_averageCalculation() {
    val reviews = listOf(
      PropertyReview(
        id = "rev_1",
        propertyId = "prop_1",
        userId = "user_1",
        userName = "Yacine B.",
        rating = 5,
        cleanlinessRating = 5,
        communicationRating = 4,
        accuracyRating = 5,
        locationRating = 5,
        comment = "Appartement très propre et spacieux.",
        rentalPeriod = "Locataire pendant 1 an"
      ),
      PropertyReview(
        id = "rev_2",
        propertyId = "prop_1",
        userId = "user_2",
        userName = "Amira K.",
        rating = 4,
        cleanlinessRating = 4,
        communicationRating = 5,
        accuracyRating = 4,
        locationRating = 5,
        comment = "Quartier calme et agréable.",
        rentalPeriod = "Séjour 8 mois"
      )
    )

    val avgRating = reviews.map { it.rating }.average()
    assertEquals(4.5, avgRating, 0.01)

    val avgCleanliness = reviews.map { it.cleanlinessRating }.average()
    assertEquals(4.5, avgCleanliness, 0.01)

    val avgComm = reviews.map { it.communicationRating }.average()
    assertEquals(4.5, avgComm, 0.01)
  }
}
