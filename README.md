Hide Mock Location is Xposed Module, which hides information about 'Allow mock locations' setting enabled.

User can choose two modes of blocking: WHITELIST MODE or BLACKLIST MODE.

In WHITELIST MODE all apps see 'Allow mock locations' setting as disabled, except for apps chosen by user, which are immune to setting spoofing.
In BLACKLIST MODE all apps see 'Allow mock locations' true setting, except for apps chosen by user, which see 'Allow mock locations' setting as disabled.

Application works in on-fly mode, which means it doesn't need to reboot to apply change of settings.
