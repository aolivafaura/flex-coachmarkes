FLEX-COACHMARKS
===============

A simple library to add a coachmarks flow to your project


![Check it out in action](assets/coachmarks_flow_showcase.gif?raw=true)

Please check the sample project for more info about the usage.

Latest version
--------
The current version is `1.0.21`

Add Jitpack repository to your root Gradle file
```
repositories {
   ...
   maven { url "https://jitpack.io" }
}
```
Add the below lines to your application Gradle file
```
dependencies {
    ...
    implementation("com.github.aolivafaura:flex-coachmarkes:1.0.21")
}
```
Features
--------
- Custom description view. Just define the XML and pass the inflated view to the library.
- Flexible positioning. You can position the description view relative to the highlighted view or relative to the screen.
- Custom close button. Just define the XML and pass the inflated view to the library.
- RTL support.
- Customizable behaviour for highlights:
  - Two different shapes, rectangular and circular.
  - Customizable velocity for the highlight animation.
  - Adjustable percentage in relation of the view highlighted width.
- Events listener to control what's happening in the flow.

