package io.codebrews.kotlinkafkadynamodemo

data class Customer(val emailAddress: String, val firstName: String, val lastName: String)

data class CustomerPersist(val customerId: String, val emailAddress: String, val firstName: String, val lastName: String)
