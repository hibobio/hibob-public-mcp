package com.hibob.service.bobConnector

const val ROOT_PATH: String = "/root/"

enum class EmployeeField(val path: String, val outputJsonKey: String) {
    EmployeeId("/root/id", "employeeId"),
    DisplayName("/root/displayName", "displayText"),
    WorkTitle("/work/title", "title"),
    Avatar("/about/avatar", "image"),
    ;

    fun jsonPath(): String = path.removePrefix(ROOT_PATH)
}
