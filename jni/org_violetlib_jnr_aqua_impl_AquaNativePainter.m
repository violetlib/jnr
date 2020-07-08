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
#import <JavaNativeFoundation.h>

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
//static const int LargeSize = 3;

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

// Special codes for non-standard segmented control styles
static const int NSSegmentStyleSeparated_Rounded = 80;
static const int NSSegmentStyleSeparated_Textured = 81;
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

static NSSegmentedControl *segmentedControl;
static NSSegmentedControl *segmentedControl4;

static const jint SEGMENT_FLAG_IS_SELECTED = 1;
static const jint SEGMENT_FLAG_IS_LEFT_NEIGHBOR_SELECTED = 2;
static const jint SEGMENT_FLAG_IS_RIGHT_NEIGHBOR_SELECTED = 4;
static const jint SEGMENT_FLAG_DRAW_LEADING_SEPARATOR = 8;
static const jint SEGMENT_FLAG_DRAW_TRAILING_SEPARATOR = 16;

static const int SEGMENT_POSITION_FIRST = 0;
static const int SEGMENT_POSITION_MIDDLE = 1;
static const int SEGMENT_POSITION_LAST = 2;
static const int SEGMENT_POSITION_ONLY = 3;

// array indexes for debugging output

static const int DEBUG_SEGMENT_WIDTH = 0;
static const int DEBUG_SEGMENT_HEIGHT = 1;
static const int DEBUG_SEGMENT_X_OFFSET = 2;
static const int DEBUG_SEGMENT_Y_OFFSET = 3;
static const int DEBUG_SEGMENT_DIVIDER_WIDTH = 4;
static const int DEBUG_SEGMENT_OUTER_LEFT_INSET = 5;
static const int DEBUG_SEGMENT_LEFT_INSET = 6;
static const int DEBUG_SEGMENT_RIGHT_INSET = 7;

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintSegmentedButton
 * Signature: ([IIIFFIIIIZI[F[I)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintSegmentedButton
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h,
    jint segmentStyle, jint segmentPosition, jint sz, jint st, jboolean isFocused, jint flags,
    jfloatArray debugOutput, jintArray debugImageData)
{
    COCOA_ENTER(env);

    BOOL isLeft = segmentPosition == SEGMENT_POSITION_ONLY || segmentPosition == SEGMENT_POSITION_FIRST;
    BOOL is1x = rw == w;

    int version = setupSegmented();

    // Map our segmented style to a native segmented style.

    // The native styles are:
    // NSSegmentStyleRounded - used for tabbed panes and for the default style
    // NSSegmentStyleRoundRect - used for the INSET style (also known as Rounded Rect)
    // NSSegmentStyleTexturedSquare - used for the other TEXTURED styles
    // NSSegmentStyleSmallSquare - used for the SMALL SQUARE style
    // NSSegmentStyleSeparated - used for the SEPARATED styles
    // NSSegmentStyleTexturedRounded - used for the discouraged TOOLBAR style
    // NSSegmentStyleCapsule - used for the discouraged SCURVE style

    jint oss = segmentStyle;

    BOOL inToolbar = NO;
    if (segmentStyle >= 1000) {
        inToolbar = YES;
        segmentStyle -= 1000;
    }

    BOOL isSeparated = segmentStyle == NSSegmentStyleSeparated_Rounded || segmentStyle == NSSegmentStyleSeparated_Textured;

    if (segmentStyle == NSSegmentStyleSeparated_Rounded) {
        segmentStyle = NSSegmentStyleSeparated;
    } else if (segmentStyle == NSSegmentStyleSeparated_Textured) {
        segmentStyle = NSSegmentStyleSeparated;
        currentWindow = fakeTexturedWindow;
    } else if (segmentStyle == NSSegmentStyleTexturedSquare) {
        currentWindow = fakeTexturedWindow;
    }

    BOOL usingTexturedWindow = (currentWindow == fakeTexturedWindow);

    //NSLog(@"nativePaintSegmentedButton style=%d %d %@ %d", segmentStyle, oss, currentWindow, currentWindow == fakeTexturedWindow);

    // The following layout parameters are hand tuned and must be kept in sync with the actual painting. They describe
    // empty space on either end of the segmented control relative to the control frame.

    // The outer left inset is the width of the empty space on the left side of the control. It affects the bounds of
    // all segments.

    // The corner inset is the extra width that is added to the first and last segment to account for rounded corners.
    // It also includes any extra blank space on either side of the control.

    // The divider inset is the extra width that is added to the all segments other than the last segment to account for the
    // divider that may be drawn for the segment (whether or not it is painted inside the segment).

    // The outer right inset is the extra space (in addition to the corner inset) added to the right side of the control.

    float outerLeftInset = 0;
    float outerRightInset = 0;
    float cornerInset = 0;
    float dividerInset = 1;

    // The divider visual width describes the layout area in which a divider is actually painted. It is used to adjust
    // the control configuration and extraction region to hide or reveal dividers. The divider visual width should be
    // zero when no divider is painted, even if the divider inset is non-zero. The divider actual visual width
    // is used in one special case where 1 pixel wide dividers were drawn on a 2x display. Both parameters are in
    // units of points.

    float dividerVisualWidth = 1;        // in integral points
    float dividerActualVisualWidth = 0;  // in points, may be fractional, zero to use dividerVisualWidth

    // The top inset adjusts vertically.

    float topInset = 0;

    // Identify the position of each divider relative to the segment boundary.
    // In 1x rendering, the divider is always to the left of the boundary.
    // In 2x rendering, there are three options.

    int LEFT = -1;  // on the left side of the boundary
    int RIGHT = 1;  // on the right side of the boundary
    int CENTER = 0; // straddling the boundary

    int dividerPosition2x = LEFT;

    if (version == SEGMENTED_10_10 || version == SEGMENTED_10_11) {

        if (oss == NSSegmentStyleRounded || oss == NSSegmentStyleSeparated_Rounded) {
            outerLeftInset = sz == MiniSize ? 1 : 2;
        } else if (oss == NSSegmentStyleRoundRect) {
            outerLeftInset = 1;
        }

        if (oss == NSSegmentStyleSeparated_Toolbar || oss == NSSegmentStyleSeparated_Textured) {
            cornerInset = sz == RegularSize ? 3 : 1;
        } else if (oss == NSSegmentStyleSmallSquare
            || oss == NSSegmentStyleTexturedSquare || oss == NSSegmentStyleTexturedSquare_Toolbar) {
            cornerInset = 1;
        } else if (oss == NSSegmentStyleTexturedRounded) {
            cornerInset = sz == RegularSize ? 3 : 1;
        } else if (oss == NSSegmentStyleRounded || oss == NSSegmentStyleSeparated_Rounded
            || oss == NSSegmentStyleCapsule || oss == NSSegmentStyleRoundRect) {
            cornerInset = 3;
        } else {
            NSLog(@"Unexpected segmented style: %d", oss);
        }

        dividerPosition2x = CENTER;

        if (oss == NSSegmentStyleSmallSquare) {
            dividerPosition2x = LEFT;
        } else if (version == SEGMENTED_10_11) {
            if (oss == NSSegmentStyleTexturedSquare || oss == NSSegmentStyleCapsule || oss == NSSegmentStyleTexturedRounded) {
                dividerPosition2x = RIGHT;
            }
        }

    } else if (version == SEGMENTED_10_13_OLD || version == SEGMENTED_10_14_OLD) {

        if (oss == NSSegmentStyleRounded || oss == NSSegmentStyleSeparated_Rounded) {
            outerLeftInset = sz == MiniSize ? 1 : 2;
        } else if (oss == NSSegmentStyleRoundRect) {
            outerLeftInset = 1;
        }

        // left is intended to match the parameter displayed as left in the explorer

        int left = 0;

        if (oss == NSSegmentStyleSeparated_Toolbar || oss == NSSegmentStyleSeparated_Textured
                    || oss == NSSegmentStyleTexturedRounded) {
            left = sz == RegularSize ? 4 : 2;
        } else if (oss == NSSegmentStyleSmallSquare || oss == NSSegmentStyleTexturedSquare
                    || oss == NSSegmentStyleTexturedSquare_Toolbar) {
            left = 2;
        } else if (oss == NSSegmentStyleRoundRect) {
            left = 3;
        } else if (oss == NSSegmentStyleCapsule) {
            left = 4;
        } else if (oss == NSSegmentStyleRounded || oss == NSSegmentStyleSeparated_Rounded) {
            left = sz == MiniSize ? 3 : 2;
        } else {
            NSLog(@"Unexpected segmented style: %d", oss);
        }

        if (version == SEGMENTED_10_14_OLD) {
            dividerPosition2x = RIGHT;
            if (is1x) {
                left++;
            }
        } else {
            dividerPosition2x = CENTER;
        }

        cornerInset = outerLeftInset + left - dividerVisualWidth;

        if (oss == NSSegmentStyleSeparated_Textured && sz == MiniSize) {
            topInset = 1;
        }

        if (!is1x) {
            if (oss == NSSegmentStyleTexturedSquare
                || oss == NSSegmentStyleTexturedRounded
                || oss == NSSegmentStyleCapsule
                ) {
                dividerPosition2x = RIGHT;
            }
        }

    } else if (version == SEGMENTED_10_13 || version == SEGMENTED_10_14) {

        if (version == SEGMENTED_10_13 && !is1x) {
            dividerActualVisualWidth = 0.5f;
        }

        if (oss == NSSegmentStyleRounded || oss == NSSegmentStyleSeparated_Rounded) {
            outerLeftInset = sz == MiniSize ? 1 : 2;
        } else if (oss == NSSegmentStyleRoundRect) {
            outerLeftInset = 1;
        }

        if (oss == NSSegmentStyleSeparated_Textured) {
            outerRightInset = 1;
        }

        if (isSeparated) {
            dividerPosition2x = CENTER;
        }

        if (oss == NSSegmentStyleSeparated_Rounded || oss == NSSegmentStyleSeparated_Textured) {
            cornerInset = sz == MiniSize ? 2 : 3;
        } else if (oss == NSSegmentStyleRoundRect) {
            cornerInset = 2;
        } else if (oss == NSSegmentStyleSmallSquare) {
            cornerInset = 1;
        } else {
            cornerInset = 3;
        }

    } else if (version == SEGMENTED_11_0) {
        if (oss == NSSegmentStyleRounded) {
            cornerInset = 2;
            outerRightInset = 2;
            dividerInset = 3;
            // A segment in the ON/Mixed ("selected") state does not display dividers
            if (st > 0) {
                dividerVisualWidth = 0;
            } else {
                dividerVisualWidth = 3;
            }
        } else if (oss == NSSegmentStyleSeparated_Rounded) {
            cornerInset = 3;
        } else if (oss == NSSegmentStyleSeparated_Toolbar || oss == NSSegmentStyleSeparated_Textured) {
            cornerInset = 1;
            dividerInset = dividerVisualWidth = 5;
            outerRightInset = 4;
        } else if (oss == NSSegmentStyleTexturedSquare || oss == NSSegmentStyleCapsule || oss == NSSegmentStyleTexturedSquare_Toolbar) {
            cornerInset = 5;
            dividerInset = 5;
            outerLeftInset = 1;
            // A segment in the ON/Mixed ("selected") state does not display dividers
            if (st > 0) {
                dividerVisualWidth = 0;
            }
        } else if (oss == NSSegmentStyleTexturedRounded) {
            // A segment in the ON/Mixed ("selected") state does not display dividers
            if (st > 0) {
                dividerVisualWidth = 0;
            }
        }
    }

    // The following parameters relate the requested width of a segment to the actual width of a segment, which is
    // often wider than requested to allow room for dividers. If we want a particular actual width, the width we
    // specify must be reduced by the extra amount defined by these parameters.

    float leftExtra = cornerInset + dividerInset - outerLeftInset;
    float middleExtra = dividerInset;
    float rightExtra = cornerInset + outerRightInset - outerLeftInset;

    //NSLog(@" displayed style %d", segmentStyle);  // debug
    //NSLog(@" button state %d, flags %d", st, flags);  // debug
    //NSLog(@" requested width %f", w); // debug

    // A segmented control with one segment is painted directly.

    // Otherwise, we create a segmented control large enough for four segments and render it into the buffer.
    // The four segments correspond to the three possible position based renderings with an option for
    // whether the middle segment is next to a selected segment or not.
    // We set the user space of the graphics context so that we capture the appropriate region in the buffer.

    // The segment width will be less than the provided width because the provided width includes
    // space for a border and/or a divider.

    // All the dividers will be painted, but only the desired ones are included in the requested bounds.

    // Right to left orientation is not supported. Not sure it needs to be.

    float otherSegmentWidth = 20;

    float segmentWidth;
    int segmentIndex;
    int selectedSegmentIndex = -1;

    float xOffset = 0;
    float widthAdjustment = 0;

    int dividerPosition = is1x ? LEFT : dividerPosition2x;

    float cw;

    if (segmentPosition == SEGMENT_POSITION_ONLY) {
        cw = w + outerLeftInset;
    } else {
        if (segmentPosition == SEGMENT_POSITION_FIRST) {
          segmentIndex = 0;
          segmentWidth = w - leftExtra;
        } else if (segmentPosition == SEGMENT_POSITION_MIDDLE) {
          segmentIndex = 1;
          segmentWidth = w - middleExtra;
        } else if (segmentPosition == SEGMENT_POSITION_LAST) {
          segmentIndex = 3;
          segmentWidth = w - rightExtra;
        } else {
          // should not happen
          @throw([NSException exceptionWithName: NSInvalidArgumentException reason: @"Invalid segment position parameter" userInfo: nil]);
        }

//        NSLog(@"Style %d: segment %d width before divider adjustment: %.1f; %.1f %.1f %.1f %d",
//            oss, segmentIndex, segmentWidth, leftExtra, middleExtra, rightExtra, dividerPosition);

        // Multiple selection is not supported

        if (flags & SEGMENT_FLAG_IS_SELECTED) {
          selectedSegmentIndex = segmentIndex;
        } else if (flags & SEGMENT_FLAG_IS_LEFT_NEIGHBOR_SELECTED) {
          if (segmentIndex > 0) {
            selectedSegmentIndex = segmentIndex - 1;
          }
        } else if (flags & SEGMENT_FLAG_IS_RIGHT_NEIGHBOR_SELECTED) {
          if (segmentIndex == 1) {
            segmentIndex = 2;
            selectedSegmentIndex = 3;
          } else if (segmentIndex == 0) {
            selectedSegmentIndex = 1;
          }
        }

        if (segmentIndex > 0) {
          // the width of the left segment is otherSegmentWidth + leftExtra
          xOffset = segmentIndex * (otherSegmentWidth + middleExtra) + (leftExtra - middleExtra);
          widthAdjustment += leftExtra;
        }

        if (segmentIndex < 3) {
          // the width of the right segment is otherSegmentWidth + rightExtra
          widthAdjustment += rightExtra;
        }

        // The next step is to adjust the segment width and extracted region location to either hide or reveal dividers,
        // as needed to satisfy the requested set of dividers. This adjustment is inhibited in the case of a
        // separated style in 2x when the "divider" is centered. What this basically means
        // is that we always want to show the entire segment, as the half divider on either side is the border or
        // space that we want to be visible.

        BOOL isSeparatedCentered2X = isSeparated && !is1x && dividerPosition == CENTER;
        if (!isSeparatedCentered2X && dividerVisualWidth > 0) {
            if (segmentIndex > 0) {
              // adjust so that the left divider is not visible by default
              if (dividerPosition != LEFT) {
                //NSLog(@"Shifting and widening to hide left divider");
                xOffset += dividerVisualWidth;
                segmentWidth += dividerVisualWidth;
                widthAdjustment += dividerVisualWidth;
              }
            }

            if (segmentIndex < 3) {
              // adjust so that the right divider is not visible by default
              if (dividerPosition != RIGHT) {
                //NSLog(@"Widening to hide right divider");
                segmentWidth += dividerVisualWidth;
                widthAdjustment += dividerVisualWidth;
              }
            }

            BOOL drawLeadingDivider = segmentIndex > 0 && (flags & SEGMENT_FLAG_DRAW_LEADING_SEPARATOR) != 0;
            BOOL drawTrailingDivider = segmentIndex < 3 && (flags & SEGMENT_FLAG_DRAW_TRAILING_SEPARATOR) != 0;
            if (drawLeadingDivider) {
              float actualVisualWidth = dividerActualVisualWidth > 0 ? dividerActualVisualWidth : dividerVisualWidth;
              xOffset -= actualVisualWidth;
              int adjustment = (int) ceil(actualVisualWidth);
              if (dividerPosition == CENTER) {
                xOffset -= dividerVisualWidth;
                adjustment += dividerVisualWidth;
              }
              segmentWidth -= adjustment;
              widthAdjustment -= adjustment;
              //NSLog(@"Shifting to reveal left divider");
            }

            if (drawTrailingDivider) {
              segmentWidth -= dividerVisualWidth;
              widthAdjustment -= dividerVisualWidth;
              if (dividerPosition == CENTER) {
                segmentWidth -= dividerVisualWidth;
                widthAdjustment -= dividerVisualWidth;
              }
              //NSLog(@"Shrinking to reveal right divider");
            }
        }

        //NSLog(@" Adjusted segment width: %.1f", segmentWidth);

        // The following computation of the control frame width seems redundant.
        // It may be conservative.

        cw = outerLeftInset + w + 3 * (dividerInset + otherSegmentWidth) + widthAdjustment;
    }

    float ch = h + topInset;
    NSRect controlFrame = NSMakeRect(0, 0, cw, ch);

    __block NSSegmentedControl *view;

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){

        if (segmentPosition == SEGMENT_POSITION_ONLY) {

            // Create and configure the segmented control
            if (segmentedControl == nil) {
                segmentedControl = [[NSSegmentedControl alloc] initWithFrame: controlFrame];
                [segmentedControl setUserInterfaceLayoutDirection: NSUserInterfaceLayoutDirectionLeftToRight];
                [segmentedControl setSegmentCount: 1];
                [segmentedControl setLabel: @"" forSegment: 0];
            }

            view = segmentedControl;

            // setting the segment width is tricky
            // zero means fit to the text, which is not what I want
            // however, w is the control width, and the control is wider than the segment

            float theSegmentWidth = w - (leftExtra + rightExtra);
            [view setWidth: theSegmentWidth forSegment: 0];
            [view setSelected: (flags & SEGMENT_FLAG_IS_SELECTED) forSegment: 0];
            [view setEnabled: isEnabled forSegment: 0];

        } else {

            // Create and configure the segmented control
            if (segmentedControl4 == nil) {
                segmentedControl4 = [[NSSegmentedControl alloc] initWithFrame: controlFrame];
                [segmentedControl4 setUserInterfaceLayoutDirection: NSUserInterfaceLayoutDirectionLeftToRight];
                [segmentedControl4 setSegmentCount: 4];
                [segmentedControl4 setLabel: @"" forSegment: 0];
                [segmentedControl4 setLabel: @"" forSegment: 1];
                [segmentedControl4 setLabel: @"" forSegment: 2];
                [segmentedControl4 setLabel: @"" forSegment: 3];
                }

            view = segmentedControl4;

            [view setWidth: otherSegmentWidth forSegment: 0];
            [view setWidth: otherSegmentWidth forSegment: 1];
            [view setWidth: otherSegmentWidth forSegment: 2];
            [view setWidth: otherSegmentWidth forSegment: 3];
            [view setWidth: segmentWidth forSegment: segmentIndex];
            [view setEnabled: isEnabled forSegment: 0];
            [view setEnabled: isEnabled forSegment: 1];
            [view setEnabled: isEnabled forSegment: 2];
            [view setEnabled: isEnabled forSegment: 3];
            [view setSelected: NO forSegment: 0];
            [view setSelected: NO forSegment: 1];
            [view setSelected: NO forSegment: 2];
            [view setSelected: NO forSegment: 3];
            if (selectedSegmentIndex >= 0) {
                [view setSelected: YES forSegment: selectedSegmentIndex];
            }
        }

        [view setFrame: controlFrame];
        installContentView(view, inToolbar);
        [view setSegmentStyle: segmentStyle];
        setControlSize(view, sz);
        setControlState(view, st);

        //    NSLog(@"Segmented control: %@ style: %ld mode: %ld width: %f",
        //          [view description],
        //          (long) view.segmentStyle,
        //          (long) view.trackingMode,
        //          cw);
        //    NSInteger segmentCount = view.segmentCount;
        //    for (NSInteger i = 0; i < segmentCount; i++) {
        //      CGFloat sw = [view widthForSegment: i];
        //      NSLog(@"  %f", sw);
        //    }

        [view layout];

        //windowDebug(currentWindow);

        NSAffineTransform* xform = [NSAffineTransform transform];
        [xform translateXBy: -(outerLeftInset + xOffset) yBy: topInset];
        [xform concat];
        displayView(view, gc, controlFrame);
    });

    int crw = 0;    // raster width in pixels
    int crh = 0;    // raster height in pixels

    if (debugImageData) {
        // If requested, return an image of the entire control.

        cw = ceil(cw);
        ch = ceil(ch);

        float xScale = rw / w;
        float yScale = rh / h;
        crw = (int) (xScale * cw);
        crh = (int) (yScale * ch);

        //NSLog(@"Creating debug image %d %d", crw, crh);

        performGraphics(env, debugImageData, crw, crh, cw, ch, ^(NSGraphicsContext *gc){
            if (usingTexturedWindow) {
                currentWindow = fakeTexturedWindow;
            }
            installContentView(view, inToolbar);
            displayView(view, gc, controlFrame);
        });
    }

    if (debugOutput) {
        jfloat *a = (*env)->GetFloatArrayElements(env, debugOutput, NULL);
        if (a) {
            a[DEBUG_SEGMENT_WIDTH] = crw;
            a[DEBUG_SEGMENT_HEIGHT] = crh;
            a[DEBUG_SEGMENT_X_OFFSET] = outerLeftInset + xOffset;
            a[DEBUG_SEGMENT_Y_OFFSET] = topInset;
            a[DEBUG_SEGMENT_DIVIDER_WIDTH] = dividerInset;
            a[DEBUG_SEGMENT_OUTER_LEFT_INSET] = outerLeftInset;
            a[DEBUG_SEGMENT_LEFT_INSET] = leftExtra;
            a[DEBUG_SEGMENT_RIGHT_INSET] = rightExtra;
            (*env)->ReleaseFloatArrayElements(env, debugOutput, a, 0);
        }
    }

    segmentedControl = nil;
    segmentedControl4 = nil;

    COCOA_EXIT(env);
}

static const int TEST_SEGMENTED_ONE_SEGMENT_MASK = (1 << 10);
static const int TEST_SEGMENTED_FOUR_SEGMENT_MASK = (1 << 11);

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeTestSegmentedButton
 * Signature: ([IIIFFIIIFF[F)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeTestSegmentedButton
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h,
    jint segmentStyle, jint option, jint sz, jfloat cw, jfloat ch, jfloat segmentWidth, jboolean isSelectAny,
    jfloatArray jDebugOutput)
{
    COCOA_ENTER(env);

    initialize();

    // Paint an entire segmented control for debugging purposes (primarily layout debugging).
    // The nominal segment width is 20 points.
    // Paint either a control with 1 segment or with 4.
    // In the 4 segment case, either one segment may be selected or any number of segments may be selected.

    jint oss = segmentStyle;

    BOOL inToolbar = NO;
    if (segmentStyle >= 1000) {
        inToolbar = YES;
        segmentStyle -= 1000;
    }

    if (segmentStyle == NSSegmentStyleSeparated_Rounded) {
        segmentStyle = NSSegmentStyleSeparated;
    } else if (segmentStyle == NSSegmentStyleSeparated_Textured) {
        segmentStyle = NSSegmentStyleSeparated;
        currentWindow = fakeTexturedWindow;
    } else if (segmentStyle == NSSegmentStyleTexturedSquare) {
        currentWindow = fakeTexturedWindow;
    }

    //NSLog(@"nativeTestSegmentedButton style=%d %d %@ %d", segmentStyle, oss, currentWindow, currentWindow == fakeTexturedWindow);

    jfloat outputData[20];
    __block jfloat *outputDataPointer = outputData;

    for (int i = 0; i < 20; i++) {
        outputData[i] = 0;
    }

    performGraphics(env, data, rw, rh, w, h, ^(NSGraphicsContext *gc){

        NSRect controlFrame = NSMakeRect(0, 0, cw, ch);
        NSSegmentedControl *view = [[NSSegmentedControl alloc] initWithFrame: controlFrame];
        installContentView(view, inToolbar);

        [view setUserInterfaceLayoutDirection: NSUserInterfaceLayoutDirectionLeftToRight];
        [view setSegmentStyle: segmentStyle];
        view.trackingMode = isSelectAny ? NSSegmentSwitchTrackingSelectAny : NSSegmentSwitchTrackingSelectOne;
        setControlSize(view, sz);

        // Create and configure the segmented control

        BOOL isOldStatus = ((option & TEST_SEGMENTED_ONE_SEGMENT_MASK) != 0) == ((option & TEST_SEGMENTED_FOUR_SEGMENT_MASK) != 0);
        BOOL isOneSegment = isOldStatus ? option == -1 : (option & TEST_SEGMENTED_ONE_SEGMENT_MASK) != 0;

        if (isOneSegment) {
            [view setSegmentCount: 1];
            [view setLabel: @"" forSegment: 0];
            [view setWidth: segmentWidth forSegment: 0];
            BOOL isSelected = !isOldStatus && (option & 1);
            [view setSelected: isSelected forSegment: 0];
        } else {
            [view setSegmentCount: 4];
            for (int i = 0; i < 4; i++) {
                [view setLabel: @"" forSegment: i];
                [view setWidth: segmentWidth forSegment: i];
                BOOL isSelected = isOldStatus ? (option == i) : (option & (1 << i)) != 0;
                [view setSelected: isSelected forSegment: i];
            }
        }

        [view sizeToFit];
        [view layout];

        //windowDebug(currentWindow);

        // It appears that the view will be painted at the bottom of the raster.
        // I do not know where this behavior comes from or whether it is guaranteed.

        double offset = h - view.frame.size.height;

        {
            NSRect frame = view.frame;
            *outputDataPointer++ = frame.origin.x;
            *outputDataPointer++ = frame.origin.y + offset;
            *outputDataPointer++ = frame.size.width;
            *outputDataPointer++ = frame.size.height;
        }

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

        displayView(view, gc, view.frame);
    });

    if (jDebugOutput) {
        jboolean isCopy = JNI_FALSE;
        float *data = (*env)->GetPrimitiveArrayCritical(env, jDebugOutput, &isCopy);
        if (data) {
            for (int i = 0; i < 20; i++) {
                data[i] = outputData[i];
            }
            (*env)->ReleasePrimitiveArrayCritical(env, jDebugOutput, data, 0);
        }
    }

    COCOA_EXIT(env);
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

static BOOL setupSlider(NSSlider *view, jfloat w, jfloat h, jint sliderType, jint sz, jint state, jint numberOfTickMarks, jint tickMarkPosition, jdouble value)
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

        setupSlider(view, w, h, sliderType, sz, state, numberOfTickMarks, tickMarkPosition, value);

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

        setupSlider(view, w, h, sliderType, sz, ActiveState, numberOfTickMarks, tickMarkPosition, value);

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
