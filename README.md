# kotivalo-screenshame

An Android application that tracks the time you spend on your phone and shames you for it.
Specifically, it tracks how much time you spend using social media applications and mobile games, and sends you shaming notifications if you exceed a certain consecutive period of usage.
The application does not track the content of your usage, only the time spent on the applications. The tracking information is only stored locally on the device.
The application is not intended to be a serious tool for tracking phone usage, but rather a fun project to experiment with Android development.

## Features / Requirements
- Track the time spent on social media applications and mobile games
- The classification of social media applications and mobile games must be pre-defined, not evaluated by AI or anything like that.
- The application setup process must be simple and not require any external services or accounts. Possible additional Android permissions are acceptable.
- The application does not need to be eligible for the Google Play Store, but it must be possible to install it on an Android device.
- Send shaming notifications if you exceed a certain consecutive period of usage
- The notifications come from a fictional character called Kotivalo, who is a person that is disappointed in you
- Kotivalo is a character from Microsoft Word stock photos, and the images to the notifications are a combination of the stock photos and AI-generated images based on the stock photos.
- AI is used to create pairs for images and responses, so that the responses are contextually relevant to the images. All responses are pre-generated and stored in the application, no external services are used to generate the responses.
- The images and responses are generated outside of the application and stored in the application as assets. The application randomly selects an image and a response from the assets when sending a notification.
