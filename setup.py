from setuptools import setup

setup(
    name="hibob-public-mcp",
    version="0.1.0",
    py_modules=["mcp-server"],
    install_requires=[
        "fastmcp",
        "requests"
    ],
    author="Your Name",
    author_email="your@email.com",
    description="HiBob MCP server integration.",
    long_description=open("README.md").read() if __import__('os').path.exists('README.md') else "",
    long_description_content_type="text/markdown",
    url="https://github.com/yourusername/hibob-public-mcp",
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires=">=3.8",
) 