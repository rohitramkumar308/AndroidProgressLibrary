# AndroidProgressLibrary
A library of custom progress views.

##Wave & Linear Progress View

![](https://github.com/rohitramkumar308/AndroidProgressLibrary/blob/master/progress_library.gif)

Customizations
```
    <declare-styleable name="WaveLoadingView">
        <attr name="radius" format="dimension" />
        <attr name="circleCount" format="integer" />
        <attr name="colors" format="reference" />
        <attr name="waveHeight" format="dimension" />
    </declare-styleable>
```

Attrs        | Description                     | Defaults    |
------------ | --------------------------------| ------------|
radius       | Radius of each circle.          | 15dp        |
circleCount  | Number of circles.              | 4           |
colors       | Array of colors for each circle.| Color.Black |
waveHeight   | Max. height a circle can move.  | 160 px      |


Note: All the above properties(except "waveHeight") will work for both the progress views.

build.gradle:

```
dependencies {
	        compile 'com.github.rohitramkumar308:AndroidProgressLibrary:-SNAPSHOT'
	}
```
