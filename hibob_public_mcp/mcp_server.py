from fastmcp import FastMCP
from typing import Literal, Union
import requests
import os

# Create a server instance
mcp = FastMCP(name="MyAssistantServer")

def _hibob_api_call(endpoint: str, body: dict = None, method: str = "POST") -> dict:
    """Helper to call the HiBob API with proper headers, supporting GET and POST."""
    url = f"https://api.hibob.com/v1/{endpoint}"
    hibob_token = os.environ.get("HIBOB_API_TOKEN", "")
    headers = {
        'authorization': f'Basic {hibob_token}',
        'content-type': 'application/json'
    }
    if method == "GET":
        response = requests.get(url, headers=headers)
    elif method == "POST":
        response = requests.post(url, json=body, headers=headers)
    elif method == "PUT":
        response = requests.put(url, json=body, headers=headers)
    elif method == "DELETE":
        response = requests.delete(url, headers=headers)
    response.raise_for_status()
    return response.json()

@mcp.tool()
def hibob_people_search(fields: list = None, filters: list = None, human_readable: Union[Literal["REPLACE"], None] = None) -> dict:
    """
    Search for employees in HiBob using advanced filters.

    Parameters:
        fields (list, optional): List of field paths to return for each employee. Use hibob_get_employee_fields to discover available fields.
        filters (list, optional): filter by ID or email. Options - 
        Example filter usage:
        filters = [
            {
                "fieldPath": "root.id",
                "operator": "equals",
                "values": ["EMPLOYEE_ID"]
            }
        ] 
        OR
        filters = [
            {
                "fieldPath": "root.email",
                "operator": "equals",
                "values": ["bla@example.com"]
            }
        ]

        to find employee by name you need to fetch with empty filters and then filter by name by yourself.
        human_readable (str, optional): Set to "REPLACE" to get human-readable values instead of machine-format.
            By default, returns machine-format values.

    To get available field paths for fields, use the hibob_get_employee_fields tool.
    """
    body = {}
    if fields:
        body["fields"] = fields
    if filters:
        body["filters"] = filters
    if human_readable == "REPLACE":
        body["humanReadable"] = "REPLACE"
    return _hibob_api_call("people/search", body)

@mcp.tool()
def hibob_get_employee_fields() -> dict:
    """
    Get metadata about all employee fields from HiBob.
    Use this tool to discover available field paths for use in filters in hibob_people_search.
    """
    return _hibob_api_call("company/people/fields", method="GET")

@mcp.tool()
def hibob_update_employee(employeeId: str, fields: dict) -> dict:
    """
    Update specific fields in an employee's record in HiBob.
    Only employee fields are supported; table updates are not allowed via this endpoint.
    Parameters:
        employeeId (string, mandatory): List of field paths to return for each employee. Use hibob_get_employee_fields to discover available fields.
        fields (dict, mandatory): object with field to value for update. example: {"root.firstName": "NewName"}
    Example usage:
        hibob_update_employee("EMPLOYEE_ID", {"root.firstName": "NewName"})
    To get available field for filters and fields, use the hibob_get_employee_fields tool.
    See: https://apidocs.hibob.com/reference/put_people-identifier
    """
    endpoint = f"people/{employeeId}"
    return _hibob_api_call(endpoint, body=fields, method="PUT")

@mcp.tool()
def hibob_get_timeoff_policy_types() -> dict:
    """
    Get a list of all timeoff policy type names from HiBob.
    See: https://apidocs.hibob.com/reference/get_timeoff-policy-types
    """
    return _hibob_api_call("timeoff/policy-types", method="GET")

@mcp.tool()
def hibob_submit_timeoff_request(employee_id: str, request_details: dict) -> dict:
    """
    Submit a new time off request for an employee in HiBob.
    
    Parameters:
        employee_id (str): The HiBob employee ID.
        request_details (dict): The request body as required by the API. See the API docs for required fields for each request type.
            Common parameters for a Holiday request include:
                - type (str): The time off type, e.g., "Holiday"
                - requestRangeType: Value must be 'days'. mandatory.
                - startDatePortion: Value must be 'all_day'. mandatory.
                - endDatePortion: Value must be 'all_day'. mandatory.
                - startDate (str): Start date in YYYY-MM-DD format
                - endDate (str): End date in YYYY-MM-DD format
                - days (float): Number of days requested
                - reason (str, optional): Reason for the request
                - comment (str, optional): Additional comments
                - halfDay (bool, optional): If the request is for a half day
                - policyType (str, optional): Policy type name
                - reasonCode (str, optional): Reason code if required by policy

            Example:
                hibob_submit_timeoff_request(
                    "EMPLOYEE_ID",
                    {
                        "type": "Holiday",
                        "startDate": "2024-07-01",
                        "endDate": "2024-07-05",
                        "startDatePortion": 'all_day',
                        "endDatePortion": 'all_day',
                        "requestRangeType": 'days',
                        "reason": "Vacation",
                        "comment": "Family trip",
                        "halfDay": False
                    }
                )
    
    See: https://apidocs.hibob.com/reference/post_timeoff-employees-id-requests
    """
    endpoint = f"timeoff/employees/{employee_id}/requests"
    return _hibob_api_call(endpoint, body=request_details, method="POST")

@mcp.tool()
def hibob_create_employee(fields: dict) -> dict:
    """
    Create a new employee record in HiBob.

    Parameters:
        fields (dict): Dictionary of employee fields to set. Only fields listed in the Fields Metadata API are allowed.
        check that you have site and startDate which are manadatory

    Example usage:
        hibob_create_employee({
            "root.firstName": "Jane",
            "root.surname": "Doe",
            "root.email": "jane.doe@example.com"
        })

    See: https://apidocs.hibob.com/reference/post_people
    """
    return _hibob_api_call("people", body=fields, method="POST")

@mcp.tool()
def hibob_get_employee_tasks(employee_id: str) -> dict:
    """
    Get all tasks for a specific employee in HiBob.

    Parameters:
        employee_id (str): The HiBob employee ID.

    Example usage:
        hibob_get_employee_tasks("EMPLOYEE_ID")

    See: https://apidocs.hibob.com/reference/get_tasks-people-id
    """
    endpoint = f"tasks/people/{employee_id}"
    return _hibob_api_call(endpoint, method="GET")

if __name__ == "__main__":
    mcp.run(transport="stdio")
