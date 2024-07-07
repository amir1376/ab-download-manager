### Note
this is a shared module that used in both `buildSrc` and my main build
here how I can add dependency to each of my composite modules
```kts
implementation("$definedGroupId:$definedProjectName:$projectVersion")
```
the benefit of this solution is two things
1. I can use shared code in both `buildSrc` and `root project's mainBuild`
2. I can move this module in separate repository without any modifications 