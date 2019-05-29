# ImageCompressor
Small lightweight library to compress images as per your requirement. 
[![](https://jitpack.io/v/rjchakraborty/ImageCompressor.svg)](https://jitpack.io/#rjchakraborty/ImageCompressor)

Compressor is a lightweight and powerful android image compression library. Compressor will allow you to compress large photos into smaller sized photos with very less or negligible loss in quality of the image.

# Gradle
To get a Git project into your build:
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

```
dependencies {
    implementation 'com.github.rjchakraborty:ImageCompressor:0.1.0'
}
```
# Let's compress the image size!
#### Compress Image to File or byte array
```java
  new ImageCompressor.Builder(activity)
                                .setConfiguration(ImageConfiguration.MEDIA_QUALITY_LOW)
                                .setImage(mFile.getAbsolutePath())
                                //.setImageOutputSize(2000.0f, 1000.0f) // if declared then wil override configuration
                                .onImageCompressed(new ImageCompressListener() {
                                    @Override
                                    public void onImageCompressed(byte[] bytes, File file) {
                                        if(file != null && file.exists()) {
                                            imageView.setImageURI(Uri.parse(file.getAbsolutePath()));
                                        }
                                    }
                                })
                                .build();
```

License
-------
    Copyright (c) 2019 WithUTechnologies.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.




