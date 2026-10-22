package ru.travelplanner.errors

import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

data class ApiError(
    val message: String
)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(error: NotFoundException): ApiError {
        return ApiError(error.message ?: "Error: Resource not found.")
    }

    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflict(error: ConflictException): ApiError {
        return ApiError(error.message ?: "Error: Resource has already been modified.")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidArgument(error: IllegalArgumentException): ApiError {
        return ApiError(error.message ?: "Error: Invalid argument.")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(error: MethodArgumentNotValidException): ApiError {
        val fields = error.bindingResult.fieldErrors.map { it.field }.distinct().joinToString(", ")
        return ApiError("Error: Invalid request fields: $fields.")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidJson(): ApiError {
        return ApiError("Error: Invalid JSON or request field type.")
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidParameter(): ApiError {
        return ApiError("Error: Invalid URL parameter.")
    }
    
    @ExceptionHandler(ExternalServiceException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun handleExternalService(error: ExternalServiceException): ApiError {
        return ApiError(error.message ?: "Error: External service request failed.")
    }
}
