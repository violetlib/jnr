/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

#import <CoreFoundation/CoreFoundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#include <math.h>
#include <stdlib.h>

#include "JNI.h"
#include "org_violetlib_jnr_aqua_impl_AquaNativePainter.h"
#include "AppearanceSupport.h"

extern Boolean _CFExecutableLinkedOnOrAfter(CFIndex);

static BOOL isActive;
static BOOL isEnabled;
static NSGraphicsContext *currentGraphicsContext;
static CGContextRef currentCGContext;
static BOOL useLayer = NO;

// Internal codes for sizes
static const int MiniSize = 0;
static const int SmallSize = 1;
static const int RegularSize = 2;
static const int LargeSize = 3;

// Internal codes for control state
static const int ActiveState = 0;
static const int InactiveState = 1;
static const int DisabledState = 2;
static const int PressedState = 3;
static const int DefaultState = 4;
static const int RolloverState = 5;
static const int DisabledInactiveState = 6;

// Codes for text field types
static const int TextFieldNormal = 0;
static const int TextFieldRound = 1;
static const int TextFieldSearch = 2;
static const int TextFieldSearchWithCancel = 3;
static const int TextFieldSearchWithMenu = 4;
static const int TextFieldSearchWithMenuAndCancel = 5;

// Special codes for non-standard slider types
static const int RightToLeftSlider = 1002;
static const int UpsideDownSlider = 1003;

// The following distinguishable versions of slider layout and rendering have been identified.
static const int SLIDER_10_10 = 0; // rendering on macOS 10.10+
static const int SLIDER_11_0 = 2;  // rendering on macOS 11.0, when linked against SDK 11.0 or later

// Special codes for non-standard segmented control styles
static const int NSSegmentStyleSeparated_Rounded = 80;
static const int NSSegmentStyleSeparated_Textured = 81;
static const int NSSegmentStyleSlider = 82;
static const int NSSegmentStyleTexturedSquare_Toolbar = 1000 + NSSegmentStyleTexturedSquare;
static const int NSSegmentStyleSeparated_Toolbar = 1000 + NSSegmentStyleSeparated_Textured;

// debug support

static NSString *createIndentation(int indent) {
    return [@"                                   " substringToIndex: indent];
}

static NSString *createColorDescription(NSColor *color)
{
    if (!color) {
        return @"";
    }
    color = [color colorUsingColorSpace: NSColorSpace.sRGBColorSpace];
    CGFloat red = color.redComponent;
    CGFloat green = color.greenComponent;
    CGFloat blue = color.blueComponent;
    CGFloat alpha = color.alphaComponent;
    if (alpha == 1) {
        return [NSString stringWithFormat: @"[%.2f %.2f %.2f]", red, green, blue];
    } else {
        return [NSString stringWithFormat: @"[%.2f %.2f %.2f %.2f]", red, green, blue, alpha];
    }
}

static NSString *createCGColorDescription(CGColorRef color)
{
    if (!color) {
        return @"";
    }
    return createColorDescription([NSColor colorWithCGColor:color]);
}

static NSString *createFrameDescription(NSRect frame)
{
    return [NSString stringWithFormat: @"[%.2f %.2f %.2f %.2f]",
        frame.origin.x, frame.origin.y, frame.size.width, frame.size.height];
}

static NSString *createLayerDescription(CALayer *layer)
{
    if (layer) {
        NSString *description = [layer debugDescription];
        NSRect frame = layer.frame;
        NSString *od = layer.opaque ? @" Opaque" : @"";
        NSString *md = layer.masksToBounds ? @" Masks" : @"";
        NSString *rd = layer.cornerRadius > 0 ? [NSString stringWithFormat: @"Corner=%.2f", layer.cornerRadius] : @"";
        NSString *cd = createCGColorDescription(layer.backgroundColor);
        NSString *fd = createFrameDescription(layer.frame);
        return [NSString stringWithFormat: @" %@%@%@ %@ %@ %@", layer, od, md, rd, cd, fd];
    } else {
        return @"";
    }
}

static NSString *createViewDescription(NSView *v)
{
    if (v) {
        NSString *description = [v description];
        if ([v isKindOfClass: [NSVisualEffectView class]]) {
            NSVisualEffectView *vv = (NSVisualEffectView*) v;
            description = [NSString stringWithFormat: @"%@ state=%ld", description, (long) vv.state];
        }
        return description;
    } else {
        return @"";
    }
}

static void viewDebug(NSView *v, NSString *title, int indent)
{
    NSString *titleString = title ? [NSString stringWithFormat: @"%@: ", title] : @"";
    NSString *od = v.opaque ? @" Opaque" : @"";
    NSString *viewDescription = createViewDescription(v);
    NSString *flippedDescription = v.flipped ? @" Flipped" : @"";
    NSString *fd = createFrameDescription(v.frame);
    NSString *indentation = createIndentation(indent);

    NSLog(@"%@%@%@%@ %@%@",
        indentation,
        titleString, viewDescription, od, fd, flippedDescription);
    if (v.layer) {
        NSString *layerDescription = createLayerDescription(v.layer);
        NSLog(@"%@  Layer: %@", indentation, layerDescription);
    }
    for (NSView *sv in v.subviews) {
        viewDebug(sv, @"", indent+2);
    }
}

static NSView *getTopView(NSWindow *w)
{
    NSView *view = w.contentView;
    while (view != nil) {
        NSView *parent = view.superview;
        if (parent == nil) {
            return view;
        }
        view = parent;
    }
    return nil;
}

static void windowDebug(NSWindow *w)
{
    NSString *od = w.opaque ? @" Opaque" : @"";
    NSString *fd = createFrameDescription(w.frame);
    NSRect frame = w.frame;
    NSLog(@"Window: %@%@ %@", [w description], od, fd);
    NSLog(@"  Background: %@", createColorDescription(w.backgroundColor));

    NSAppearance *appearance = w.appearance;
    if (appearance) {
        NSLog(@"  Appearance: %@", [appearance name]);
    }
    appearance = w.effectiveAppearance;
    if (appearance) {
        NSLog(@"  Effective appearance: %@", [appearance name]);
    }

    NSView *v = getTopView(w);
    if (v != nil) {
        viewDebug(v, @"", 2);
    }
}

//
// FakeParentWindow
// Allows control over the active/inactive status
// Does not resize its subview
// Can simulate being a toolbar
//

@interface FakeParentWindow : NSWindow
- (int) _semanticContext;
- (instancetype) initWithStyle:(NSWindowStyleMask)style;
- (void) setView: (NSView *) view;
- (void) clear;
- (void) setToolbar: (BOOL) isToolbar;
@end

// Support for views that display differently in toolbars

@interface NSView (NSViewPrivate)
- (int)_semanticContext;
@end

@interface NSWindow (NSWindowPrivate)
- (int)_semanticContext;
- (BOOL)_shouldUseTexturedAppearanceForSegmentedCellInView: (NSView *)view;
- (NSView *)_borderView;
@end

@interface MyContentView : NSView
@property BOOL isToolbar;
- (int) _semanticContext;
- (void) setView: (NSView *) view;
- (void) clear;
@end

@implementation FakeParentWindow

- (instancetype) initWithStyle:(NSWindowStyleMask)style
{
    NSRect frame = NSMakeRect(0, 0, 10000, 10000);
    self = [super initWithContentRect: frame
                            styleMask: style
                              backing: NSBackingStoreNonretained
                                defer: YES];

    if (self) {
        self.contentView = [[MyContentView alloc] initWithFrame: frame];
    }
    return self;
}

- (int) _semanticContext
{
    MyContentView *contentView = (MyContentView *) self.contentView;
    return [contentView _semanticContext];
}

- (BOOL) _shouldUseTexturedAppearanceForSegmentedCellInView: (NSView *)view
{
    MyContentView *contentView = (MyContentView *) self.contentView;
    return contentView.isToolbar || (self.styleMask & NSWindowStyleMaskTexturedBackground);
}

- (void) setView: (NSView *) view
{
    MyContentView *contentView = (MyContentView *) self.contentView;
    [contentView setView: view];
}

- (void) clear
{
    MyContentView *contentView = (MyContentView *) self.contentView;
    [contentView clear];
}

- (void) setToolbar: (BOOL) isToolbar
{
    MyContentView *contentView = (MyContentView *) self.contentView;
    contentView.isToolbar = isToolbar;
}

- (BOOL)isKeyWindow
{
    return isActive;
}

- (BOOL)hasKeyAppearance
{
    return isActive;
}

- (void) displayIfNeeded
{
}
@end

@interface MyPanel : NSPanel
@end

@implementation MyPanel
- (BOOL)isKeyWindow
{
    return isActive;
}
- (BOOL)hasKeyAppearance
{
    return isActive;
}
@end

@implementation MyContentView

- (instancetype) initWithFrame: (NSRect) frameRect
{
    self = [super initWithFrame: frameRect];
    if (self) {
        self.autoresizesSubviews = NO;
    }
    return self;
}

- (int) _semanticContext
{
    if (!_isToolbar) {
        return 0;
    }
    if (@available(macOS 10.14, *)) {
        return 4;
    } else {
        return 5;
    }
}

- (void) setView: (NSView *) view
{
    [self clear];
    [self addSubview: view];
}

- (void) clear
{
    for (;;) {
        NSArray<NSView *> *subviews = self.subviews;
        if (subviews.count > 0) {
            NSView *view = subviews[0];
            [view removeFromSuperview];
        } else {
            break;
        }
    }
}

@end

@interface NSSegmentedCell (JNRPrivate)
- (NSRect) _rectForSegment:(NSInteger)segment inFrame:(NSRect)frame;
@end

static FakeParentWindow *currentWindow;
static FakeParentWindow *fakeParentWindow;
static FakeParentWindow *fakeTexturedWindow;

static FakeParentWindow *fakeDocumentWindow;    // used only for title bars
static MyPanel *myPanel;    // used only for title bars

static jint osVersion;

static void runOnMainThread(void (^block)())
{
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:block];
}

static void initOnMainThread()
{
    NSOperatingSystemVersion version = [[NSProcessInfo processInfo] operatingSystemVersion];
    osVersion = (version.majorVersion * 100 + version.minorVersion) * 100 + version.patchVersion;

    if (!fakeParentWindow) {
        fakeParentWindow = [[FakeParentWindow alloc] initWithStyle: NSWindowStyleMaskBorderless];
        currentWindow = fakeParentWindow;
    }

    // A textured window background is needed for textured separated segmented controls and
    // for inactive textured segmented controls.
    if (!fakeTexturedWindow) {
        fakeTexturedWindow = [[FakeParentWindow alloc] initWithStyle: NSWindowStyleMaskTexturedBackground];
    }

    if (!fakeDocumentWindow) {
        fakeDocumentWindow = [[FakeParentWindow alloc] initWithStyle:
        (NSWindowStyleMaskTitled|NSWindowStyleMaskClosable|NSWindowStyleMaskMiniaturizable|NSWindowStyleMaskResizable)];
    }

    if (!myPanel) {
        NSRect rect = NSMakeRect(0, 0, 10000, 10000);
        myPanel = [[MyPanel alloc] initWithContentRect: rect
            styleMask: (NSWindowStyleMaskUtilityWindow|NSWindowStyleMaskTitled|NSWindowStyleMaskClosable|NSWindowStyleMaskMiniaturizable|NSWindowStyleMaskResizable)
            backing: NSBackingStoreNonretained
            defer: YES
            ];
        myPanel.contentView = [[MyContentView alloc] initWithFrame: rect];
    }
}

static void initialize()
{
    runOnMainThread(^(){initOnMainThread();});
}

static void installContentView(NSView *view, BOOL inToolbar)
{
    [currentWindow setToolbar: inToolbar];
    [currentWindow setView: view];
    if (view) {
        currentWindow.appearance = configuredAppearance;
        [currentWindow displayIfNeeded];
    }
}

static void ensureWindowSize(float width, float height)
{
    NSRect frame = currentWindow.frame;
    float fudge = 30;
    width += fudge;
    height += fudge;
    if (frame.size.width < width || frame.size.height < height) {
        frame = NSMakeRect(0, 0, width, height);
        [currentWindow setFrame: frame display: NO];
    }
}

static NSGraphicsContext *setupRaw(int *data, int rw, int rh, int w, int h)
{
  // The NSBitmapImageRep approach creates an immutable raster unless you let it allocate its own memory.

  //rasterBuffer = (unsigned char *) data;

//     NSBitmapImageRep *bmpImageRep = [[NSBitmapImageRep alloc]
//                                       initWithBitmapDataPlanes:NULL
//                                       pixelsWide:rw
//                                       pixelsHigh:rh
//                                       bitsPerSample:8
//                                       samplesPerPixel:4
//                                       hasAlpha:YES
//                                       isPlanar:NO
//                                       colorSpaceName:NSCalibratedRGBColorSpace
//                                       bitmapFormat:NSAlphaFirstBitmapFormat
//                                       bytesPerRow:0
//                                       bitsPerPixel:0
//                                       ];
//     // There isn't a colorspace name constant for sRGB so retag
//     // using the sRGBColorSpace method
//     bmpImageRep = [bmpImageRep bitmapImageRepByRetaggingWithColorSpace: [NSColorSpace sRGBColorSpace]];
//     // Setting the user size communicates the dpi
//     [bmpImageRep setSize:NSMakeSize(w, h)];
//     // Create a bitmap context
//     currentGraphicsContext = [NSGraphicsContext graphicsContextWithBitmapImageRep:bmpImageRep];

  CGColorSpaceRef colorspace = CGColorSpaceCreateDeviceRGB();
  currentCGContext = CGBitmapContextCreate(data, rw, rh, 8, rw * 4, colorspace, kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Host);
  CGColorSpaceRelease(colorspace);

  ensureWindowSize(w, h);

  float xscale = ((float) rw) / w;
  float yscale = ((float) rh) / h;
  CGContextScaleCTM(currentCGContext, xscale, yscale);

  if (@available(macOS 10.10, *)) {
    currentGraphicsContext = [NSGraphicsContext graphicsContextWithCGContext:currentCGContext flipped:NO];
  } else {
    currentGraphicsContext = [NSGraphicsContext graphicsContextWithGraphicsPort:currentCGContext flipped:NO];
  }

  [NSGraphicsContext saveGraphicsState];
  [NSGraphicsContext setCurrentContext: currentGraphicsContext];
  initOnMainThread();

  return currentGraphicsContext;
}

static void cleanup()
{
  if (currentGraphicsContext) {
    [NSGraphicsContext restoreGraphicsState];
    currentGraphicsContext = NULL;
  }

  if (currentCGContext) {
    CGContextRelease(currentCGContext);
    currentCGContext = NULL;
  }

  if (fakeParentWindow) {
    [fakeParentWindow clear];
  }

  if (fakeTexturedWindow) {
    [fakeTexturedWindow clear];
  }

  currentWindow = fakeParentWindow;
}

static void performGraphicsRaw(int *data, jint rw, jint rh, jfloat w, jfloat h,
    void (^block)(NSGraphicsContext *gc))
{
    runOnMainThread(^(){
        NSGraphicsContext *gc = setupRaw(data, rw, rh, w, h);
        if (gc) {
            block(gc);
            cleanup();
        }
    });
}

static void performGraphics(JNIEnv *env, jintArray pixelData, jint rw, jint rh, jfloat w, jfloat h,
    void (^block)(NSGraphicsContext *gc))
{
    jboolean isCopy = JNI_FALSE;
    int *rawPixelData = (*env)->GetPrimitiveArrayCritical(env, pixelData, &isCopy);
    if (rawPixelData) {
        performGraphicsRaw(rawPixelData, rw, rh, w, h, block);
        (*env)->ReleasePrimitiveArrayCritical(env, pixelData, rawPixelData, 0);
    }
}

static void setControlSize(NSView* v, int sz)
{
    // Large not supported

    NSControlSize size = NSControlSizeRegular;

    switch (sz)
    {
        case MiniSize:
            size = NSControlSizeMini;
            break;
        case SmallSize:
            size = NSControlSizeSmall;
            break;
        case LargeSize:
            if (osVersion >= 101600) {
                size = 3;
            }
    }

    if ([v respondsToSelector: @selector(setControlSize:)]) {
        [(id)v setControlSize: size];
    } else {
        if ([v respondsToSelector: @selector(cell)]) {
            NSCell *cell = [(id)v cell];
            [(id)cell setControlSize: size];
        } else {
            //NSLog(@"setControlSize: is not defined on %@", v);
        }
    }

    if ([v respondsToSelector: @selector(setFont:)]) {
        [(id)v setFont: [NSFont systemFontOfSize: [NSFont systemFontSizeForControlSize: size]]];
    }
}

static void setControlState(NSView* v, int st)
{
  isActive = YES;
  isEnabled = YES;

  switch (st)
  {
    case ActiveState:
      break;
    case InactiveState:
      isActive = NO;
      break;
    case DisabledState:
      isEnabled = NO;
      break;
    case DisabledInactiveState:
      isEnabled = NO;
      isActive = NO;
      break;
    case PressedState:
      break;
    case DefaultState:
      break;
    case RolloverState:
      break;
  }

  if ([v respondsToSelector: @selector(setEnabled:)]) {
    [(id)v setEnabled: isEnabled];
  }
}

static void displayView(NSView *view, NSGraphicsContext *gc, NSRect frameRect)
{
    if (useLayer && view.layer) {
        [view.layer display];
        [view.layer renderInContext: gc.CGContext];
    } else {
        [view displayRectIgnoringOpacity: frameRect inContext: gc];
    }
}

static void displayViewPreferringLayer(NSView *view, NSGraphicsContext *gc, NSRect frameRect)
{
    if (view.layer) {
        [view.layer display];
        [view.layer renderInContext: gc.CGContext];
    } else {
        [view displayRectIgnoringOpacity: frameRect inContext: gc];
    }
}

static int sliderVersion = -1;

static int setupSlider()
{
    if (sliderVersion >= 0) {
        return sliderVersion;
    }

    initialize();

    if (osVersion < 101600) {
        sliderVersion = SLIDER_10_10;
    } else {
        Boolean isNewStyle = _CFExecutableLinkedOnOrAfter(11);
        sliderVersion = isNewStyle ? SLIDER_11_0 : SLIDER_10_10;
    }

    return sliderVersion;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    isLayerPaintingEnabled
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_isLayerPaintingEnabled
  (JNIEnv *env, jclass cl)
{
    return useLayer;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    setLayerPaintingEnabled
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_setLayerPaintingEnabled
  (JNIEnv *env, jclass cl, jboolean b)
{
    useLayer = b;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintIndeterminateProgressBar
 * Signature: ([IIIIIIIIZI)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintIndeterminateProgressIndicator
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint sz, jint st, jint o, jboolean isSpinner, jint frame)
{
    COCOA_ENTER(env);

    ensureWindowSize(2*w, h);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSProgressIndicator* view = [[NSProgressIndicator alloc] initWithFrame: frameRect];
        if (isSpinner) {
            [view setStyle: NSProgressIndicatorSpinningStyle];
        } else {
            [view setIndeterminate: YES];
        }
        installContentView(view, NO);
        setControlSize(view, sz);
        setControlState(view, st);
        displayView(view, gc, frameRect);

        // A crude simulation of animation frames

        if (frame > 0) {
            jint theFrame = frame % 10;
            NSRectClip(NSMakeRect(5, 0, w-10, h));
            NSAffineTransform* xform = [NSAffineTransform transform];
            [xform translateXBy: -w + 40 + theFrame * w/15 yBy: 0];
            [xform concat];
            frameRect = NSMakeRect(0, 0, 2*w, h);
            [view setFrame: frameRect];
            [view displayRectIgnoringOpacity: frameRect inContext: gc];
        }
    });

  COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintProgressBar
 * Signature: ([IIIIIIIID)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintProgressIndicator
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint sz, jint st, jint o, jdouble v)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSProgressIndicator* view = [[NSProgressIndicator alloc] initWithFrame: frameRect];
        installContentView(view, NO);
        setControlSize(view, sz);
        setControlState(view, st);
        [view setIndeterminate: NO];
        [view setMaxValue: 1];
        [view setDoubleValue: v];
        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
}

static NSButton *buttonView;

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintButton
 * Signature: ([IIIIIIIIIZI)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintButton
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h,
    jint buttonType, jint bezelStyle, jint sz, jint st, jboolean isFocused, jint value, jint layoutDirection)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){

        NSRect frameRect = NSMakeRect(0, 0, w, h);

        if (buttonView == NULL) {
            buttonView = [[NSButton alloc] initWithFrame: frameRect];
        } else {
            [buttonView setFrame: frameRect];
        }

        BOOL isHighlight = NO;
        BOOL allowsMixedState = NO;
        BOOL shouldPaint = YES;
        BOOL inToolbar = NO;

        jint theBezelStyle = bezelStyle;
        if (theBezelStyle >= 1000) {
            inToolbar = YES;
            theBezelStyle -= 1000;
        }

        installContentView(buttonView, inToolbar);

        jint theValue = value;
        jint theButtonType = buttonType;
        if (theValue == 2) {
            allowsMixedState = YES;
            theValue = -1;
        } else if (theValue == 1) {
            if (theBezelStyle != NSRoundedBezelStyle && theBezelStyle != NSHelpButtonBezelStyle
            && theButtonType != NSSwitchButton && theButtonType != NSRadioButton && theButtonType != NSPushOnPushOffButton) {
                //isHighlight = YES;
                //theValue = 0;
                theButtonType = NSPushOnPushOffButton;
            }
        } else {
            theValue = 0;
        }

        [currentWindow setDefaultButtonCell: NULL];

        if (st == DefaultState) {

            [currentWindow setDefaultButtonCell: [buttonView cell]];

        } else if (st == PressedState) {

            isHighlight = YES;

        }

        if (shouldPaint) {
            setControlSize(buttonView, sz);
            setControlState(buttonView, st);
            [buttonView setButtonType: theButtonType];
            [buttonView setBezelStyle: theBezelStyle];
            [buttonView setAllowsMixedState: allowsMixedState];
            [buttonView setState: theValue];
            [buttonView highlight: isHighlight];
            [buttonView setTitle: @""];
            [[buttonView cell] setUserInterfaceLayoutDirection: layoutDirection];
            displayView(buttonView, gc, frameRect);
        }

        // TBD: Is this an AppKit bug? Once set to NSSwitchButton or NSBRadioButton,
        // further changes have no effect.
        if (theButtonType == NSSwitchButton || theButtonType == NSRadioButton) {
            buttonView = NULL;
        }

    });

  COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintColorWell
 * Signature: ([IIIFFI)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintColorWell
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint st)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSColorWell *view = [[NSColorWell alloc] initWithFrame: frameRect];
        installContentView(view, NO);
        setControlState(view, st);
        [view setIntegerValue: st == PressedState];        // does not work
        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
}

// The following distinguishable versions of segmented control layout and rendering have been identified.
// Note that the stock JDK up to Java 11 (and possibly later) is linked against a 10.9 SDK.

static const int SEGMENTED_10_10 = 0;           // rendering on macOS 10.10
static const int SEGMENTED_10_11 = 1;           // rendering on macOS 10.11 and 10.12
static const int SEGMENTED_10_13_OLD = 2;       // rendering on macOS 10.13 that is similar to 10.11, used when linked against an old SDK
static const int SEGMENTED_10_13 = 3;           // a unique rendering on macOS 10.13, when linked against SDK 10.11 or later
static const int SEGMENTED_10_14_OLD = 4;       // rendering on macOS 10.14 that is similar to 10.11, used when linked against an old SDK
static const int SEGMENTED_10_14 = 5;           // rendering on macOS 10.14, when linked against SDK 10.11 or later
static const int SEGMENTED_11_0 = 6;            // rendering on macOS 11.0, when linked against SDK 11.0 or later

// Note that 11.0 was originally known as 10.16.

// The key differences:
//
// All renderings except SEGMENTED_10_13 use dividers that are visually 1 point wide.
// That means the dividers are two pixels wide in 2x.
// SEGMENTED_10_13 uses dividers that are visually 1 pixel wide, but are allocated 1 point of width.
// In all cases, the width of middle segments are increased by 1 point to account for the divider(s).
// The width of the first and last segments are generally increased by more than 1 point, to account
// for rounded corners. In all cases, the first segment gets one extra point of width, to account
// for the divider at its right edge.
//
// In 1x, dividers are positioned to the left of the segment boundary.
// The positioning of dividers in 2x varies. Sometimes they are to the left of the segment boundary,
// sometimes to the right, sometimes straddling the boundary.
//
// When a segment is selected, it may be painted with a special background color.
// The background color is painted over the divider on either side.
// As a divider may be partially or completely in the space of the adjacent segment,
// some adjustment is needed. Several approaches are used:
//
// 1. The layout is not changed. The painted background is wider than the segment.
// 2. The layout is changed so that both dividers are part of the selected segment.
// 3. The adjacent dividers are not painted. The background therefore does not need to be adjusted.
//
// The key difference in SEGMENTED_11_0 is that the basic tab/exclusive segmented style is more like separated.

static int segmentedVersion = -1;

static int setupSegmented()
{
    if (segmentedVersion >= 0) {
        return segmentedVersion;
    }

    initialize();

    if (osVersion < 101100) {
        segmentedVersion = SEGMENTED_10_10;
    } else if (osVersion < 101300) {
        segmentedVersion = SEGMENTED_10_11;
    } else if (osVersion < 101600) {
        Boolean isNewStyle = _CFExecutableLinkedOnOrAfter(11);
        if (osVersion < 101400) {
            segmentedVersion = isNewStyle ? SEGMENTED_10_13 : SEGMENTED_10_13_OLD;
        } else {
            segmentedVersion = isNewStyle ? SEGMENTED_10_14 : SEGMENTED_10_14_OLD;
        }
    } else {
        segmentedVersion = SEGMENTED_11_0;
    }

    return segmentedVersion;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintSegmentedControl1
 * Signature: ([IIIFFIZIII[F)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintSegmentedControl1
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat scale,
  jfloat w, jint style, jboolean isSelected, jint context, jint sz, jint st, jfloatArray jDebugOutput)
{
    jint result = 0;

    COCOA_ENTER(env);

    setupSegmented();

    if (style == NSSegmentStyleSeparated_Rounded) {
        style = NSSegmentStyleSeparated;
    } else if (style == NSSegmentStyleSeparated_Textured) {
        style = NSSegmentStyleSeparated;
        currentWindow = fakeTexturedWindow;
    } else if (style == NSSegmentStyleTexturedSquare) {
        currentWindow = fakeTexturedWindow;
    }

    BOOL usingTexturedWindow = (currentWindow == fakeTexturedWindow);
    BOOL isToolbar = (context == org_violetlib_jnr_aqua_impl_AquaNativePainter_CONTEXT_TOOLBAR);

    float cw = rw / scale;
    float ch = rh / scale;
    NSRect controlFrame = NSMakeRect(0, 0, cw, ch);

    jfloat outputData[4];
    __block jfloat *outputDataPointer = NULL;

    if (jDebugOutput) {
        outputDataPointer = outputData;
    }

    performGraphics(env, data, rw, rh, cw, ch, ^(NSGraphicsContext *gc){

        NSSegmentedControl *view = [[NSSegmentedControl alloc] initWithFrame: controlFrame];

        setControlSize(view, sz);
        setControlState(view, st);

        [view setUserInterfaceLayoutDirection: NSUserInterfaceLayoutDirectionLeftToRight];
        view.trackingMode = NSSegmentSwitchTrackingSelectAny;
        [view setSegmentCount: 1];
        [view setLabel: @"" forSegment: 0];
        [view setWidth: w forSegment: 0];
        [view setEnabled: isEnabled forSegment: 0];
        [view setSelected: isSelected forSegment: 0];

        [view setFrame: controlFrame];
        installContentView(view, isToolbar);
        [view setSegmentStyle: style];
        [view layout];

        if (outputDataPointer) {
            NSArray<NSView *> *subviews = [view subviews];
            if (subviews.count > 0) {
                NSRect frame = subviews[0].frame;
                *outputDataPointer++ = frame.origin.x;
                *outputDataPointer++ = frame.origin.y;
                *outputDataPointer++ = frame.size.width;
                *outputDataPointer++ = frame.size.height;
            } else if (view.segmentCount > 0) {
                NSRect frame = [view.cell _rectForSegment: 0 inFrame: view.frame];
                *outputDataPointer++ = frame.origin.x;
                *outputDataPointer++ = frame.origin.y;
                *outputDataPointer++ = frame.size.width;
                *outputDataPointer++ = frame.size.height;
            }
        }

        displayView(view, gc, controlFrame);
    });

    if (jDebugOutput) {
        jboolean isCopy = JNI_FALSE;
        float *data = (*env)->GetPrimitiveArrayCritical(env, jDebugOutput, &isCopy);
        if (data) {
            for (int i = 0; i < 4; i++) {
                data[i] = outputData[i];
            }
            (*env)->ReleasePrimitiveArrayCritical(env, jDebugOutput, data, 0);
        }
    }

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintSegmentedControl4
 * Signature: ([IIIFFFFFIIIIII[F)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintSegmentedControl4
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat scale,
  jfloat sw1, jfloat sw2, jfloat sw3, jfloat sw4,
  jint style, jint tracking, jint selectionFlags, jint context, jint sz, jint st, jfloatArray jDebugOutput)
{
    jint result = 0;

    COCOA_ENTER(env);

    setupSegmented();

    if (style == NSSegmentStyleSeparated_Rounded) {
        style = NSSegmentStyleSeparated;
    } else if (style == NSSegmentStyleSeparated_Textured) {
        style = NSSegmentStyleSeparated;
        currentWindow = fakeTexturedWindow;
    } else if (style == NSSegmentStyleTexturedSquare) {
        currentWindow = fakeTexturedWindow;
    }

    BOOL usingTexturedWindow = (currentWindow == fakeTexturedWindow);
    BOOL isToolbar = (context == org_violetlib_jnr_aqua_impl_AquaNativePainter_CONTEXT_TOOLBAR);

    float cw = rw / scale;
    float ch = rh / scale;
    NSRect controlFrame = NSMakeRect(0, 0, cw, ch);

    jfloat outputData[16];
    __block jfloat *outputDataPointer = NULL;

    if (jDebugOutput) {
        outputDataPointer = outputData;
    }

    performGraphics(env, data, rw, rh, cw, ch, ^(NSGraphicsContext *gc){

        NSSegmentedControl *view = [[NSSegmentedControl alloc] initWithFrame: controlFrame];

        setControlSize(view, sz);
        setControlState(view, st);

        [view setUserInterfaceLayoutDirection: NSUserInterfaceLayoutDirectionLeftToRight];
        view.trackingMode = tracking;
        [view setSegmentCount: 4];
        [view setLabel: @"" forSegment: 0];
        [view setLabel: @"" forSegment: 1];
        [view setLabel: @"" forSegment: 2];
        [view setLabel: @"" forSegment: 3];
        [view setWidth: sw1 forSegment: 0];
        [view setWidth: sw2 forSegment: 1];
        [view setWidth: sw3 forSegment: 2];
        [view setWidth: sw4 forSegment: 3];
        [view setEnabled: isEnabled forSegment: 0];
        [view setEnabled: isEnabled forSegment: 1];
        [view setEnabled: isEnabled forSegment: 2];
        [view setEnabled: isEnabled forSegment: 3];
        [view setSelected: (selectionFlags & org_violetlib_jnr_aqua_impl_AquaNativePainter_SELECT_SEGMENT_1) != 0 forSegment: 0];
        [view setSelected: (selectionFlags & org_violetlib_jnr_aqua_impl_AquaNativePainter_SELECT_SEGMENT_2) != 0 forSegment: 1];
        [view setSelected: (selectionFlags & org_violetlib_jnr_aqua_impl_AquaNativePainter_SELECT_SEGMENT_3) != 0 forSegment: 2];
        [view setSelected: (selectionFlags & org_violetlib_jnr_aqua_impl_AquaNativePainter_SELECT_SEGMENT_4) != 0 forSegment: 3];

        [view setFrame: controlFrame];
        installContentView(view, isToolbar);
        [view setSegmentStyle: style];
        [view layout];

        if (outputDataPointer) {
            NSArray<NSView *> *subviews = [view subviews];
            if (subviews.count > 0) {
                int viewCount = subviews.count > 4 ? 4 : subviews.count;
                for (int i = 0; i < viewCount; i++) {
                    NSRect frame = subviews[i].frame;
                    *outputDataPointer++ = frame.origin.x;
                    *outputDataPointer++ = frame.origin.y;
                    *outputDataPointer++ = frame.size.width;
                    *outputDataPointer++ = frame.size.height;
                }
            } else {
                int segmentCount = view.segmentCount > 4 ? 4 : view.segmentCount;
                for (int i = 0; i < segmentCount; i++) {
                    NSRect frame = [view.cell _rectForSegment: i inFrame: view.frame];
                    *outputDataPointer++ = frame.origin.x;
                    *outputDataPointer++ = frame.origin.y;
                    *outputDataPointer++ = frame.size.width;
                    *outputDataPointer++ = frame.size.height;
                }
            }
        }

        displayView(view, gc, controlFrame);
    });

    if (jDebugOutput) {
        jboolean isCopy = JNI_FALSE;
        float *data = (*env)->GetPrimitiveArrayCritical(env, jDebugOutput, &isCopy);
        if (data) {
            for (int i = 0; i < 16; i++) {
                data[i] = outputData[i];
            }
            (*env)->ReleasePrimitiveArrayCritical(env, jDebugOutput, data, 0);
        }
    }

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeDetermineSliderRenderingVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeDetermineSliderRenderingVersion
  (JNIEnv *env, jclass cl)
{
    jint result = -1;

    COCOA_ENTER(env);

    result = setupSlider();

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeDetermineSegmentedButtonRenderingVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeDetermineSegmentedButtonRenderingVersion
    (JNIEnv *env, jclass cl)
{
    jint result = -1;

    COCOA_ENTER(env);

    result = setupSegmented();

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeDetermineSegmentedButtonFixedHeight
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeDetermineSegmentedButtonFixedHeight
  (JNIEnv *env, jclass cl, jint segmentStyle, jint sz)
{
    __block jint result = -1;

    COCOA_ENTER(env);

    runOnMainThread(^(){
        initOnMainThread();

        float originalWidth = 1000;
        float originalHeight = 1000;
        NSRect frameRect = NSMakeRect(0, 0, originalWidth, originalHeight);
        NSSegmentedControl* view = [[NSSegmentedControl alloc] initWithFrame: frameRect];

        [view setSegmentStyle: segmentStyle];
        [view setSegmentCount: 1];
        setControlSize(view, sz);
        [view setLabel: @"Text" forSegment: 0];
        [view sizeToFit];
        result = [view bounds].size.height;        // all segmented controls are fixed height

        // For unknown reasons, this height is too small!
    });

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeDetermineSegmentedButtonLayoutParameters
 * Signature: (II[F)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeDetermineSegmentedButtonLayoutParameters
  (JNIEnv *env, jclass cl, jint segmentStyle, jint sz, jfloatArray jData)
{
    jint originalSegmentStyle = segmentStyle;

    //NSLog(@"Called nativeDetermineSegmentedButtonLayoutParameters: %d", originalSegmentStyle);

    __block jint result = -1;

    COCOA_ENTER(env);

    BOOL inToolbar = NO;
    if (segmentStyle >= 1000) {
        inToolbar = YES;
        segmentStyle -= 1000;
    }

    BOOL isTextured = NO;
    if (segmentStyle == NSSegmentStyleSeparated_Rounded) {
        segmentStyle = NSSegmentStyleSeparated;
    } else if (segmentStyle == NSSegmentStyleSeparated_Textured) {
        segmentStyle = NSSegmentStyleSeparated;
        isTextured = YES;
    } else if (segmentStyle == NSSegmentStyleTexturedSquare) {
        isTextured = YES;
    }

    __block float leftInset;
    __block float rightInset;
    __block float dividerWidth;
    __block float hasSubviews;

    runOnMainThread(^(){
        initOnMainThread();

        if (isTextured) {
          currentWindow = fakeTexturedWindow;
        }

        ensureWindowSize(1000, 1000);

        NSSegmentedControl* view = [[NSSegmentedControl alloc] initWithFrame: NSMakeRect(0, 0, 500, 500)];
        installContentView(view, inToolbar);

        [view setSegmentStyle: segmentStyle];
        setControlSize(view, sz);
        [view setSegmentCount: 3];
        [view setWidth: 64 forSegment: 0];
        [view setWidth: 64 forSegment: 1];
        [view setWidth: 64 forSegment: 2];
        [view sizeToFit];
        [view layout];

        //NSLog(@"Segmented control cell view: %@", [view.cell controlView]);
        //NSLog(@"Segmented control cell has drawing overrides: %d", [view.cell _controlOrCellhasDrawingOverrides:view]);
        //NSLog(@"Segmented control uses item views: %d", [view.cell _usesItemViews]);

        //windowDebug(currentWindow);

        float width0 = 0;
        float width1 = 0;
        float width2 = 0;

        NSArray<NSView *> *subviews = [view subviews];

        if (subviews.count >= 3) {
            hasSubviews = 1;
            width0 = subviews[0].frame.size.width;
            width1 = subviews[1].frame.size.width;
            width2 = subviews[2].frame.size.width;
        } else {
            width0 = [view.cell _rectForSegment: 0 inFrame: view.frame].size.width;
            width1 = [view.cell _rectForSegment: 1 inFrame: view.frame].size.width;
            width2 = [view.cell _rectForSegment: 2 inFrame: view.frame].size.width;
        }

        float extraFirst = width0 - [view widthForSegment: 0];
        float extraMiddle = width1 - [view widthForSegment: 1];
        float extraLast = width2 - [view widthForSegment: 2];
        float unexplained = view.frame.size.width - (width0 + width1 + width2);

        // When no segment is selected, the dividers are assigned to the right edges of the segments, except the
        // last segment.

        dividerWidth = extraMiddle;
        leftInset = extraFirst - extraMiddle;
        rightInset = extraLast;

        // debug
        //NSLog(@"Segmented control layout: %.1f %.1f %.1f %.1f", leftInset, dividerWidth, rightInset, unexplained);

        cleanup();
        result = 0;
    });

    if (!result) {
        jboolean isCopy = JNI_FALSE;
        float *data = (*env)->GetPrimitiveArrayCritical(env, jData, &isCopy);
        if (data) {
            data[0] = leftInset;
            data[1] = dividerWidth;
            data[2] = rightInset;
            data[3] = hasSubviews;
            (*env)->ReleasePrimitiveArrayCritical(env, jData, data, 0);
        } else {
            result = 1;
        }
    }

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeDetermineButtonFixedHeight
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeDetermineButtonFixedHeight
  (JNIEnv *env, jclass cl, jint buttonType, jint bezelStyle, jint sz)
{
    __block jint result = -1;

    COCOA_ENTER(env);

    // Test the button using two different titles.
    // If the height is the same both times, then the height must be fixed.

    runOnMainThread(^(){
        float originalWidth = 1000;
        float originalHeight = 1000;
        NSRect frameRect = NSMakeRect(0, 0, originalWidth, originalHeight);
        NSButton* view = [[NSButton alloc] initWithFrame: frameRect];
        setControlSize(view, sz);
        [view setButtonType: buttonType];
        [view setBezelStyle: bezelStyle];
        [view setTitle: @""];
        [view sizeToFit];
        float h1 = [view bounds].size.height;
        [view setTitle: @"Horse"];
        [view sizeToFit];
        float h2 = [view bounds].size.height;
        if (h1 != originalHeight && h2 == h1) {
            result = h1;
        }
    });

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeDetermineButtonFixedWidth
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeDetermineButtonFixedWidth
  (JNIEnv *env, jclass cl, jint buttonType, jint bezelStyle, jint sz)
{
    __block jint result = -1;

    COCOA_ENTER(env);

    // I do not know how to do an unbiased test, since any button that displays a title
    // will probably change its width based on the title. But we do not use a title, so for
    // us some buttons have a fixed width.

    // This test uses knowledge of the platform that certain types of buttons have a
    // fixed width when no title is used.

    runOnMainThread(^(){

        if (bezelStyle == NSHelpButtonBezelStyle || buttonType == NSSwitchButton || buttonType == NSRadioButton) {
            float originalWidth = 1000;
            float originalHeight = 1000;
            NSRect frameRect = NSMakeRect(0, 0, originalWidth, originalHeight);
            NSButton* view = [[NSButton alloc] initWithFrame: frameRect];
            setControlSize(view, sz);
            [view setButtonType: buttonType];
            [view setBezelStyle: bezelStyle];
            [view setTitle: @""];
            [view sizeToFit];
            float w = [view bounds].size.width;
            if (w != originalWidth) {
                result = w;
            }
        }

    });

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintComboBox
 * Signature: ([IIIIIIII)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintComboBox
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint type, jint sz, jint st, jint bezelStyle, jint layoutDirection)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){

        // TBD: pressed and inactive states are not drawn correctly

        BOOL inToolbar = NO;

        jint theBezelStyle = bezelStyle;
        if (theBezelStyle >= 1000) {
            inToolbar = YES;
            theBezelStyle -= 1000;
        }

        // Note: as of 10.11.3, NSTexturedComboBox always uses the toolbar rendering. Thus a textured non-toolbar combo box
        // is clipped at the top and bottom.

        // Define the width of the region to paint when painting the indicator only or the arrows only.
        // Note that these widths may include an inset.
        int indicatorWidth = 21;
        if (sz == MiniSize) {
            indicatorWidth = 16;
        } else if (sz == SmallSize) {
            indicatorWidth = 19;
        }

        jboolean drawTextBackground = st != DisabledState && st != DisabledInactiveState;

        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSComboBox* view;

        if (theBezelStyle == NSTexturedRoundedBezelStyle) {
            Class c = NSClassFromString(@"NSTexturedComboBox");
            //NSLog(@"Instantiating class %@", c);
            view = [[c alloc] initWithFrame: frameRect];
            //NSLog(@"Cell is %@", view.cell);
            Class cc = NSClassFromString(@"NSTexturedComboBoxCell");
            //NSLog(@"Instantiating class %@", cc);
            NSCell *cell = [[cc alloc] init];
            //NSLog(@"Cell is %@", cell);
            view.cell = cell;
            view.stringValue = @"";
        } else {
            view = [[NSComboBox alloc] initWithFrame: frameRect];
        }

        installContentView(view, inToolbar);
        setControlSize(view, sz);
        setControlState(view, st);
        [view setButtonBordered: type != 2 /* ARROWS_ONLY */ ];
        [[view cell] setUserInterfaceLayoutDirection: layoutDirection];
        [view setDrawsBackground: drawTextBackground];
        //[buttonView highlight: st == PressedState];            // does not work
        //[buttonView setIntegerValue: st == PressedState];        // does not work
        if (type > 0) {
            // INDICATOR_ONLY or ARROWS_ONLY
            NSUserInterfaceLayoutDirection dir = [view userInterfaceLayoutDirection];
            NSRectClip(dir == NSUserInterfaceLayoutDirectionLeftToRight ? NSMakeRect(w - indicatorWidth, 0, indicatorWidth, h) : NSMakeRect(0, 0, indicatorWidth, h));
        }
        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintPopUpButton
 * Signature: ([IIIIIZZIIZ)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintPopUpButton
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jboolean isUp,
    jint sz, jint st, jint bezelStyle, jint layoutDirection)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSPopUpButton* view = [[NSPopUpButton alloc] initWithFrame: frameRect pullsDown: !isUp];

        BOOL inToolbar = NO;

        jint theBezelStyle = bezelStyle;
        if (theBezelStyle >= 1000) {
            inToolbar = YES;
            theBezelStyle -= 1000;
        }

        installContentView(view, inToolbar);

        // theBezelStyle 0 is for cells (borderless)
        // A recessed button border should be drawn only in the rollover state.
        // Instead, a darker border is painted in all states.

        jint size = sz;

        jboolean isBordered = !(theBezelStyle == 0 || (theBezelStyle == NSRecessedBezelStyle && st != RolloverState));
        if (theBezelStyle == 0) {
            theBezelStyle = NSShadowlessSquareBezelStyle;
            // mini arrows not supported separately
            if (size == MiniSize) {
                size = SmallSize;
            }
        }

        jint buttonType = NSMomentaryPushInButton;
        if (theBezelStyle == NSRoundedBezelStyle) {
            buttonType = NSMomentaryLightButton;
        } else if (buttonType == NSRecessedBezelStyle) {
            buttonType = NSPushOnPushOffButton;
        }

        setControlSize(view, size);
        setControlState(view, st);
        [[view cell] setBordered: isBordered];
        [[view cell] setUserInterfaceLayoutDirection: layoutDirection];

        //[view highlight: st == PressedState];            // does not work
        //[view setIntegerValue: st == PressedState];    // does not work

        [view setBezelStyle: theBezelStyle];
        [view setButtonType: buttonType];
        [[view cell] setArrowPosition: NSPopUpArrowAtBottom];
        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintToolBarItemWell
 * Signature: ([IIIIIIZ)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintToolBarItemWell
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint state, jboolean isFrameOnly)
{
  // TBD
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintGroupBox
 * Signature: ([IIIIIIZ)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintGroupBox
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint titlePosition, jint state, jboolean isFrameOnly)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSBox* view = [[NSBox alloc] initWithFrame: frameRect];
        installContentView(view, NO);
        setControlState(view, state);
        [view setTitlePosition: titlePosition];
        [view setTitle: @""];
        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
    }

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintListBox
 * Signature: ([IIIIIIZZ)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintListBox
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint state, jboolean isFocused, jboolean isFrameOnly)
{
}

@interface MySearchFieldCell : NSSearchFieldCell
@end

@implementation MySearchFieldCell
- (NSRect)searchButtonRectForBounds:(NSRect)rect
{
    // The superclass value may describe the first frame of an animated transition.
    // We want the last frame. Setting X to zero does the job.
    NSRect r = [super searchButtonRectForBounds:rect];
    return NSMakeRect(0, r.origin.y, r.size.width, r.size.height);
}
@end

@interface MySearchField : NSSearchField
@end

@implementation MySearchField
+ (Class) cellClass
{
    return [MySearchFieldCell class];
}
@end

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintTextField
 * Signature: ([IIIIIIIZZI)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintTextField
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint sz, jint state, jint type)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSTextField* view;

        BOOL inToolbar = NO;
        jint theType = type;
        if (theType >= 1000) {
            inToolbar = YES;
            theType -= 1000;
        }

        // In OS X 10.11 can do [searchField setCentersPlaceholder: NO];

        // TBD: The cancel button is too dark. Do not know why.

        if (theType >= TextFieldSearch) {
            NSSearchField *searchField = [[MySearchField alloc] initWithFrame: frameRect];
            view = searchField;
            NSSearchFieldCell *cell = [searchField cell];

            // having some text in the search field causes the cancel button to be displayed
            if (theType == TextFieldSearchWithCancel || theType == TextFieldSearchWithMenuAndCancel) {
                [view setStringValue: @" "];
            } else if (@available(macOS 10.14, *)) {
                // In 10.14, if there is no text, the placeholder is painted.
                // Having text inhibits the placeholder, but also causes the cancel button to be displayed.
                // Setting the cancel button cell to transparent fixes that.
                [view setStringValue: @" "];
                NSButtonCell *cbcell = [cell cancelButtonCell];
                cbcell.transparent = YES;
            }

            if (theType == TextFieldSearchWithMenu || theType == TextFieldSearchWithMenuAndCancel) {
                // show the menu icon
                // TBD: this is not working (it works in 10.14)
                // Probably need to field to be focused
                NSMenu *cellMenu = [[NSMenu alloc] initWithTitle: @"Dummy"];
                NSMenuItem *item = [[NSMenuItem alloc] initWithTitle:@"Clear" action:NULL keyEquivalent:@""];
                [item setTag:NSSearchFieldClearRecentsMenuItemTag];
                [cellMenu insertItem:item atIndex:0];
                [cell setSearchMenuTemplate:cellMenu];
            }

        } else {
            view = [[NSTextField alloc] initWithFrame: frameRect];
            if (theType == TextFieldNormal) {
            [view setBezelStyle: NSTextFieldSquareBezel];
            } else if (theType == TextFieldRound) {
            [view setBezelStyle: NSTextFieldRoundedBezel];
            }
        }

        installContentView(view, inToolbar);

        setControlSize(view, sz);
        setControlState(view, state);

        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
}

@interface MyTableDelegate : NSObject <NSTableViewDataSource, NSTableViewDelegate>
@end

@implementation MyTableDelegate

- (NSInteger)numberOfRowsInTableView:(NSTableView *)tableView {
    return 0;
}

- (id)tableView:(NSTableView *)aTableView objectValueForTableColumn:(NSTableColumn *)aTableColumn row:(NSInteger)rowIndex
{
    return @"x";
}

- (NSView *)tableView:(NSTableView *)tableView
    viewForTableColumn:(NSTableColumn *)tableColumn
                  row:(NSInteger)row {

    // Get an existing cell with the MyView identifier if it exists
    NSTextField *result = [tableView makeViewWithIdentifier:@"MyView" owner:self];

    // There is no existing cell to reuse so create a new one
    if (result == nil) {

        // Create the new NSTextField with a frame of the {0,0} with the width of the table.
        // Note that the height of the frame is not really relevant, because the row height will modify the height.
        result = [[NSTextField alloc] initWithFrame: NSMakeRect(0, 0, 100, 0)];

        // The identifier of the NSTextField instance is set to MyView.
        // This allows the cell to be reused.
        result.identifier = @"MyView";
    }

    // result is now guaranteed to be valid, either as a reused cell
    // or as a new cell, so set the stringValue of the cell to the
    // nameArray value at row
    result.stringValue = @"";

    // Return the result
    return result;
}

- (void)tableView:(NSTableView *)aTableView sortDescriptorsDidChange:(NSArray *)oldDescriptors
{
}

@end

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintTableColumnHeader
 * Signature: ([IIIIIIIZ)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintTableColumnHeader
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint state, jint direction, jboolean isSelected, jint layoutDirection)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){

        // Create a table view large enough to display the header cell and render it into the buffer.
        // Note that a header is displayed only if the table is contained in a scroll view.
        // We need to know where to find the header within the table view.

        // The design requires that cell dividers on both sides be rendered into the buffer.
        // It does not allow a top border to be rendered into the buffer.

        float leftInset = 12;    // prevent the fake first column from being rendered into the buffer
        float topInset = 4.49;    // prevent the top border from being rendered into the buffer
        int cellWidth = w - 4;

        int cw = w + 1 + 20;
        int ch = h + 9;
        NSRect controlFrame = NSMakeRect(0, 0, cw, ch);

        // Create and configure the table view

        NSTableColumn *firstColumn = [[NSTableColumn alloc] initWithIdentifier: @"first"];
        [firstColumn setWidth: 10];
        [[firstColumn headerCell] setStringValue: @""];

        NSTableColumn *column = [[NSTableColumn alloc] initWithIdentifier: @"foo"];
        [column setWidth: cellWidth];
        NSTableHeaderCell *cell = [column headerCell];
        [cell setStringValue: @""];

        NSRect tableFrame = NSMakeRect(0, 0, cw, ch);
        NSTableView *table = [[NSTableView alloc] initWithFrame: tableFrame];
        [table addTableColumn: firstColumn];
        [table addTableColumn: column];

        if (isSelected) {
            NSIndexSet *indexes = [NSIndexSet indexSetWithIndex:1];
            [table selectColumnIndexes:indexes byExtendingSelection:NO];
        } else {
            [table deselectColumn: 1];
        }

        MyTableDelegate *d = [[MyTableDelegate alloc] init];
        [table setDelegate: d];
        [table setDataSource: d];
        [table reloadData];

        if (direction == 1) {
            [table setIndicatorImage: [NSImage imageNamed: @"NSAscendingSortIndicator"] inTableColumn: column];
        } else if (direction == 2) {
            [table setIndicatorImage: [NSImage imageNamed: @"NSDescendingSortIndicator"] inTableColumn: column];
        }

        //NSScrollView *container = [[NSScrollView alloc] initWithFrame: controlFrame];
        //[container setDocumentView: table];
        //setControlState(container, state);

        setControlState(table, state);

        [cell setUserInterfaceLayoutDirection: layoutDirection];    // not working

        NSView *view = [table headerView];

        installContentView(view, NO);

        // Set the user space so that the header cell is rendered in the top left corner
        // The bottom-up coordinate system makes this code confusing...
        NSAffineTransform* xform = [NSAffineTransform transform];
        [xform translateXBy: -leftInset yBy: h - ch + topInset];
        [xform concat];

        displayView(view, gc, controlFrame);
    });

    COCOA_EXIT(env);
}

static BOOL setupSliderView(NSSlider *view, jfloat w, jfloat h, jint sliderType, jint sz, jint state, jint numberOfTickMarks, jint tickMarkPosition, jdouble value)
{
    NSSliderCell *cell = [view cell];

    [view setMinValue: 0];
    [view setMaxValue: 1];

    setControlSize(view, sz);
    setControlState(view, state);

    // Note: in Yosemite, setting the UI layout direction using setUserInterfaceLayoutDirection: has no effect.
    // Also, there is no option for an upside-down vertical scroller

    BOOL isMirrored = NO;

    if (sliderType == RightToLeftSlider) {
        sliderType = 0;

        NSAffineTransform* xform = [NSAffineTransform transform];
        [xform scaleXBy: -1 yBy: 1];
        [xform translateXBy: -w yBy: 0];
        [xform concat];
        isMirrored = YES;

    } else if (sliderType == UpsideDownSlider) {
        sliderType = 0;

        NSAffineTransform* xform = [NSAffineTransform transform];
        [xform scaleXBy: 1 yBy: -1];
        [xform translateXBy: 0 yBy: -h];
        [xform concat];
        isMirrored = YES;
    }

    [cell setSliderType: sliderType];
    [view setNumberOfTickMarks: numberOfTickMarks];
    [view setTickMarkPosition: tickMarkPosition];
    [view setDoubleValue: value];

    // There does not seem to be any way to implement the Pressed state

    [cell setHighlighted: NO];
    return isMirrored;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintSlider
 * Signature: ([IIIIIIIIZDII)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintSlider
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint sliderType, jint sz, jint state,
    jboolean isFocused, jdouble value, jint numberOfTickMarks, jint tickMarkPosition)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSSlider* view = [[NSSlider alloc] initWithFrame: frameRect];
        installContentView(view, NO);

        setupSliderView(view, w, h, sliderType, sz, state, numberOfTickMarks, tickMarkPosition, value);

        displayView(view, gc, frameRect);
    });
    COCOA_EXIT(env);
}

@interface ThumbCapturingSliderCell : NSSliderCell
- (NSRect) getCapturedBounds;
@end

@implementation ThumbCapturingSliderCell
{
NSRect bounds;
}
- (void)drawKnob:(NSRect)knobRect
{
    bounds = knobRect;
}
- (NSRect) getCapturedBounds
{
    return bounds;
}
@end

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeGetSliderThumbBounds
 * Signature: ([FIIIIDII)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeGetSliderThumbBounds
  (JNIEnv *env, jclass cl, jfloatArray bounds, jfloat w, jfloat h, jint sliderType, jint sz, jdouble value, jint numberOfTickMarks, jint tickMarkPosition)
{
    COCOA_ENTER(env);

    __block NSRect thumbBounds;

    int *data = calloc(w * h, sizeof(int));
    performGraphicsRaw(data, w, h, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSSlider* view = [[NSSlider alloc] initWithFrame: frameRect];
        installContentView(view, NO);

        ThumbCapturingSliderCell *cell = [[ThumbCapturingSliderCell alloc] init];
        [view setCell: cell];

        setupSliderView(view, w, h, sliderType, sz, ActiveState, numberOfTickMarks, tickMarkPosition, value);

        displayView(view, gc, frameRect);

        thumbBounds = [cell getCapturedBounds];
    });

    jboolean isCopy = JNI_FALSE;
    float *boundsData = (*env)->GetPrimitiveArrayCritical(env, bounds, &isCopy);
    boundsData[0] = thumbBounds.origin.x;
    boundsData[1] = thumbBounds.origin.y;
    boundsData[2] = thumbBounds.size.width;
    boundsData[3] = thumbBounds.size.height;

    if (sliderType == RightToLeftSlider) {
        boundsData[0] = -boundsData[0] + w - boundsData[2];
    } else if (sliderType == UpsideDownSlider) {
        boundsData[1] = -boundsData[1] + h - boundsData[3];
    }

    (*env)->ReleasePrimitiveArrayCritical(env, bounds, boundsData, 0);
    free(data);

    COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintSpinnerArrows
 * Signature: ([IIIIIIIZZ)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintSpinnerArrows
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint sz, jint state, jboolean isFocused, jboolean isPressedTop)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSStepper* view = [[NSStepper alloc] initWithFrame: frameRect];
        installContentView(view, NO);

        setControlSize(view, sz);
        setControlState(view, state);

        [[view cell] setHighlighted: state == PressedState];
        if (state == PressedState && isPressedTop) {
            [view setIntValue: 1]; // nothing is working so far...
            // I suppose an alternative would be to flip the graphics...
            NSAffineTransform* xform = [NSAffineTransform transform];
            [xform scaleXBy: 1 yBy: -1];
            [xform translateXBy: 0 yBy: -h];
            [xform concat];
        }

        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintSplitPaneDivider
 * Signature: ([IIIIIIIII)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintSplitPaneDivider
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint type, jint state, jint o, jint thickness)
{
    // the thickness of standard dividers cannot be changed, so we ignore the thickness parameter

    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSSplitView *splitView = [[NSSplitView alloc] initWithFrame: frameRect];
        NSRect subviewFrame = NSMakeRect(0, 0, w, h);
        NSView *view1 = [[NSView alloc] initWithFrame: subviewFrame];
        NSView *view2 = [[NSView alloc] initWithFrame: subviewFrame];
        [splitView addSubview:view1];
        [splitView addSubview:view2];
        [splitView setPosition: 0 ofDividerAtIndex: 0];

        installContentView(splitView, NO);

        setControlState(splitView, state);
        [splitView setDividerStyle: type];
        [splitView setVertical: o];
        displayView(splitView, gc, frameRect);
    });

    COCOA_EXIT(env);
}

static void configureTitleBarButton(NSButton *b, int buttonState)
{
    // A title bar button can display as active even when the window is inactive simply
    // by rolling over the button area. The title bar itself continues to display as
    // inactive. I'm not sure if there is a way to make this happen through the API.

    if (b) {
        BOOL isActive = NO;
        BOOL isHighlight = NO;

        switch (buttonState) {
          case PressedState:
            isHighlight = YES;
            // fall through
          case ActiveState:
          case DefaultState:
          case RolloverState:
            isActive = YES;
        }

        [b setEnabled: isActive];
        [[b cell] setHighlighted: isHighlight];
    }
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintTitleBar
 * Signature: ([IIIIIIIIIIZ)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintTitleBar
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint type, jint state,
    jint closeState, jint minimizeState, jint resizeState, jboolean resizeIsFullScreen, jboolean isDirty)
{
    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        installContentView(nil, NO);

        isActive = state == ActiveState;    // only Active and Inactive are supported

        NSWindow *window = type == 0 ? fakeDocumentWindow : myPanel;
        [window setFrame: frameRect display:NO];
        [window setDocumentEdited: isDirty];

        // Surprisingly, when highlighted, the buttons also paint the icons.

        NSWindowCollectionBehavior behavior = [window collectionBehavior];
        if (resizeIsFullScreen) {
            behavior |= NSWindowCollectionBehaviorFullScreenPrimary;
        } else {
            behavior &= ~NSWindowCollectionBehaviorFullScreenPrimary;
        }
        [window setCollectionBehavior:behavior];

        // We can force a button to display as inactive.

        NSButton *minimizeButton = [window standardWindowButton: NSWindowMiniaturizeButton];
        if (minimizeButton) {
            configureTitleBarButton(minimizeButton, minimizeState);
        }

        NSButton *resizeButton = [window standardWindowButton: NSWindowZoomButton];
        if (resizeButton) {
            configureTitleBarButton(resizeButton, resizeState);
            // Not clear it is possible to get the window to paint a full screen exit icon.
            // Therefore, we display no icon.
            [[resizeButton cell] setHighlighted: NO];
        }

        // Displaying the window does not work, but displaying the super view does.

        NSButton *closeButton = [window standardWindowButton: NSWindowCloseButton];
        if (closeButton) {
            configureTitleBarButton(closeButton, closeState);
            //NSView *top = getTopView(closeButton.window);
            displayView(closeButton.superview, gc, frameRect);
        }
    });

    COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeGetTitleBarButtonLayoutInfo
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeGetTitleBarButtonLayoutInfo
  (JNIEnv *env, jclass cl, jint type)
{
    __block jintArray result = NULL;

    COCOA_ENTER(env);

    runOnMainThread(^(){
        initOnMainThread();
        int h = 100;
        NSRect frameRect = NSMakeRect(0, 0, 400, h);
        NSWindow *w = type == 0 ? fakeDocumentWindow : myPanel;
        [w setFrame: frameRect display:NO];
        isActive = true;

        NSButton *closeButton = [w standardWindowButton: NSWindowCloseButton];
        NSButton *minimizeButton = [w standardWindowButton: NSWindowMiniaturizeButton];
        NSButton *resizeButton = [w standardWindowButton: NSWindowZoomButton];

        if (closeButton) {
            configureTitleBarButton(closeButton, ActiveState);
        }

        if (minimizeButton) {
            configureTitleBarButton(minimizeButton, ActiveState);
        }

        if (resizeButton) {
            configureTitleBarButton(resizeButton, ActiveState);
        }

        int rawData[12];

        if (closeButton) {
            int n = 0;
            NSRect frame = [closeButton frame];
            rawData[n+0] = frame.origin.x;
            rawData[n+1] = closeButton.superview.frame.size.height - frame.origin.y - frame.size.height;
            rawData[n+2] = frame.size.width;
            rawData[n+3] = frame.size.height;
        }

        if (minimizeButton) {
            int n = 4;
            NSRect frame = [minimizeButton frame];
            rawData[n+0] = frame.origin.x;
            rawData[n+1] = minimizeButton.superview.frame.size.height - frame.origin.y - frame.size.height;
            rawData[n+2] = frame.size.width;
            rawData[n+3] = frame.size.height;
        }

        if (resizeButton) {
            int n = 8;
            NSRect frame = [resizeButton frame];
            rawData[n+0] = frame.origin.x;
            rawData[n+1] = resizeButton.superview.frame.size.height - frame.origin.y - frame.size.height;
            rawData[n+2] = frame.size.width;
            rawData[n+3] = frame.size.height;
        }

        jintArray data = (*env)->NewIntArray(env, 12);
        (*env)->SetIntArrayRegion(env, data, 0, 12, rawData);
        result = data;
    });

    COCOA_EXIT(env);

    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintScrollBar
 * Signature: ([IIIIIIIIFF)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintScrollBar
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h,
    jint type, jint sz, jint st, jfloat thumbPosition, jfloat thumbExtent)
{
    // For an overlay scroller, NSScroller paints a track.
    // This is appropriate for the rollover display, but not for the initial display.
    // However, the track is likely to be the wrong size, because what we get here is the
    // first frame of an "expansion" animation.

    if (type == 1 /* OVERLAY */) {
        return;
    }

    // In Yosemite, the display of an overlay scroll bar does not depend upon state.
    // A legacy scroll bar displays as an empty track when disabled.

    if (type == 2 /* OVERLAY_ROLLOVER */) {
        st = ActiveState;
    }

    if ((0)) {
        // Testing
        float width1 = [NSScroller scrollerWidthForControlSize: NSControlSizeRegular scrollerStyle: NSScrollerStyleOverlay];
        float width2 = [NSScroller scrollerWidthForControlSize: NSControlSizeSmall scrollerStyle: NSScrollerStyleOverlay];
        float width3 = [NSScroller scrollerWidthForControlSize: NSControlSizeMini scrollerStyle: NSScrollerStyleOverlay];
        float width4 = [NSScroller scrollerWidthForControlSize: NSControlSizeRegular scrollerStyle: NSScrollerStyleLegacy];
        float width5 = [NSScroller scrollerWidthForControlSize: NSControlSizeSmall scrollerStyle: NSScrollerStyleLegacy];
        float width6 = [NSScroller scrollerWidthForControlSize: NSControlSizeMini scrollerStyle: NSScrollerStyleLegacy];
        NSLog(@"scroller width = %f %f %f %f %f %f", width1, width2, width3, width4, width5, width6);
    }

    // Only Regular and Small sizes are supported

    COCOA_ENTER(env);

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){
        NSRect frameRect = NSMakeRect(0, 0, w, h);
        NSScroller* view = [[NSScroller alloc] initWithFrame: frameRect];
        installContentView(view, NO);

        switch (type)
        {
          case 0: /* LEGACY */
            [view setScrollerStyle: NSScrollerStyleLegacy];
            break;

          case 1: /* OVERLAY */
          case 2: /* OVERLAY_ROLLOVER */

            [view setScrollerStyle: NSScrollerStyleOverlay];
            [[view cell] setHighlighted: YES];
            break;
        }

        setControlSize(view, sz);
        setControlState(view, st);
        [view setFloatValue: thumbPosition];
        [view setKnobProportion: thumbExtent];
        displayView(view, gc, frameRect);
    });

    COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_NativeSupport
 * Method:    getJavaRuntimeSupportVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_violetlib_jnr_aqua_impl_NativeSupport_getJavaRuntimeSupportVersion
  (JNIEnv *env, jclass cl)
{
    jstring result = NULL;

    if ((0)) {
        // debug
        NSLog(@"User interface layout direction: %ld", (long) [[NSApplication sharedApplication] userInterfaceLayoutDirection]);
    }

    NSString *path = @"/System/Library/Frameworks/JavaVM.framework/Frameworks/JavaRuntimeSupport.framework/Versions/Current/Resources/Info.plist";
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile: path];
    if (dict) {
        NSString *s = (NSString*) [dict objectForKey: @"CFBundleShortVersionString"];
        if (s) {
            result = JNFNSToJavaString(env, s);
        }
    }
    return result;
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_NativeSupport
 * Method:    syslog
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_NativeSupport_syslog
  (JNIEnv *env, jclass cl, jstring msg)
{
    jsize slen = (*env) -> GetStringLength(env, msg);
    const jchar *schars = (*env) -> GetStringChars(env, msg, NULL);
    CFStringRef s = CFStringCreateWithCharacters(NULL, schars, slen);
    NSLog(@"%@", s);
    CFRelease(s);
    (*env) -> ReleaseStringChars(env, msg, schars);
}
