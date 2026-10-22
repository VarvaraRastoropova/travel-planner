package ru.travelplanner.errors

class NotFoundException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)

class ExternalServiceException(message: String) : RuntimeException(message)
