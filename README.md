# cpilint_custom

Custom linting rules for SAP Cloud Platform Integration (CPI) using the cpilint framework.

## Overview

This project provides custom linting rules for CPI (Cloud Platform Integration) iFlows, extending the cpilint framework with organization-specific validation rules for Ferring's integration patterns.

## Features

The custom rules include:

- **Default Names Not Allowed Rule**: Validates that iFlow components don't use default naming conventions
- **Naming Pattern Validation**: Ensures iFlows follow the Ferring naming conventions (FER_*)
- **Security Rules**: 
  - Disallows cleartext basic authentication
  - Requires encrypted data store writes
  - Requires encrypted endpoints
  - CSRF protection enforcement with configurable exclusions
- **Adapter Restrictions**: Controls which sender and receiver adapters are allowed
- **Scripting Language Restrictions**: Disallows certain scripting languages (e.g., JavaScript)
- **Header Validation**: Validates allowed and response headers
- **Documentation Requirements**: Enforces iFlow description requirements
- **Parameter Validation**: Detects unused parameters and undeclared data types

## Configuration

The custom rules are configured in `FerringRules.xml`, which defines the specific validation rules and exclusions for the Ferring integration patterns.

## Project Structure

```
cpilint_custom/
├── src/
│   ├── com/nmp/cpilint/impl/     # Custom rule implementations
│   ├── META-INF/                  # Service provider configuration
│   └── resources/                 # XQuery and other resources
├── FerringRules.xml               # Main rules configuration
└── RulesExampleExtension.xml      # Example extension rules
```

## Building

This is an Eclipse Java project. To build:

1. Import the project into Eclipse
2. Ensure Java build path is properly configured
3. Build using Eclipse's Java builder

## Usage

These custom rules integrate with the cpilint framework to validate CPI integration packages during development and CI/CD processes.

## Last Updated

**2025-12-29**
