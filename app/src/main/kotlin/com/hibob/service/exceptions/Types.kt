package com.hibob.service.exceptions

open class TranslatedException(open val key: String, open vararg val args: Any) : RuntimeException()

class NotFoundException(override val key: String, override vararg val args: Any) : TranslatedException(key, *args)

class BadInputException(override val key: String, override vararg val args: Any) : TranslatedException(key, *args)

class ForbiddenException(override val key: String, override vararg val args: Any) : TranslatedException(key, *args)

class UnauthorizedException(override val key: String, override vararg val args: Any) : TranslatedException(key, *args)
