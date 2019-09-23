# UrlTrigger Plugin

## Declarative Pipeline Syntax
Example:

```groovy
pipeline {
    
    agent any
    
    triggers {
    
        URLTrigger( 
            cronTabSpec: '* * * * *',
            entries: [
                URLTriggerEntry( 
                    url: 'http://www.mysite.com/jsoncontent',
                    username: 'myuser',
                    password: 'mypassword',
                    checkETag: false,
                    checkStatus: true,
                    statusCode: 403,
                    checkLastModificationDate: true,
                    timeout: 200,
                    requestHeaders: [
                        RequestHeader( headerName: "Accept" , headerValue: "application/json" )
                    ],
                    contentTypes: [
                        JsonContent(
                            [
                                JsonContentEntry( jsonPath: 'level1.level2.level3' )
                            ])
                    ]
                ),
                URLTriggerEntry( 
                    url: 'http://www.mysite.com/xmlcontent',
                    requestHeaders: [
                        RequestHeader( headerName: "Accept" , headerValue: "application/xml" )
                    ],
                    contentTypes: [
                        XMLContent(
                            [
                                XMLContentEntry( xPath: 'level1/level2/level3' )
                            ])
                    ]
                ),
                URLTriggerEntry( 
                    url: 'http://www.mysite.com/textcontent',
                    contentTypes: [
                        TextContent(
                            [
                                TextContentEntry( regEx: "Hello.*" ),
                                TextContentEntry( regEx: "Goodbye.*" )
                            ])
                    ]
                ),
                URLTriggerEntry( 
                    url: 'http://www.mysite.com/generalcontent',
                    contentTypes: [
                        MD5Sum()
                    ]
                )
            ]
        )
    }
    stages {
        stage( "Default stage" ) {
            steps {
                echo "This is a stage"
            }
        }
    }
}
```
