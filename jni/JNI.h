#define COCOA_ENTER(env)											\
{																	\
    @autoreleasepool {												\
	@try {

#define COCOA_EXIT(env)												\
	} @catch(NSException *localException) {							\
		[JNFException throwToJava:env exception:localException];	\
	}																\
	}																\
}
