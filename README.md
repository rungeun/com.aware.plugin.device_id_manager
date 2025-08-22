AWARE: Device ID Manager
========================

This plugin allows users to manage and customize their AWARE device identifier. It provides a simple interface to view the current device ID and change it to a custom value or generate a new UUID.

# Settings
- **status_plugin_device_id_manager**: (boolean) activate/deactivate device ID manager plugin
- **custom_device_id**: (string) custom device identifier to use for this device

# Features
- View current device ID
- Set custom device ID
- Generate new UUID automatically
- Update device ID in AWARE framework settings
- Log device ID changes

# Providers
This plugin does not have its own content provider. All device ID changes are stored directly in the AWARE framework's main settings table.
