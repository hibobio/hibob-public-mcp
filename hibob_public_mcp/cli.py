from .mcp_server import mcp

def main():
    mcp.run(transport="stdio")