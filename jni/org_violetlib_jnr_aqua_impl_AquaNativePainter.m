/*
 * Copyright (c) 2015-2016 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

#include <jni.h>

#import <CoreFoundation/CoreFoundation.h>
#import <CoreGraphics/CoreGraphics.h>
#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation.h>

#include <math.h>
#include <stdlib.h>

#include "org_violetlib_jnr_aqua_impl_AquaNativePainter.h"
#include "JNI.h"

static BOOL isActive;
static BOOL isEnabled;
static jintArray pixelData;
static void *rawPixelData;
//static unsigned char *rasterBuffer;
static NSGraphicsContext *currentGraphicsContext;
static CGContextRef currentCGContext;

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

// debug support

static NSString *createIndentation(int indent) {
  return [@"                                   " substringToIndex: indent];
}

static NSString *createViewDescription(NSView *v) {
  if (v) {
    NSString *description = [v debugDescription];
    if ([v isKindOfClass: [NSVisualEffectView class]]) {
      NSVisualEffectView *vv = (NSVisualEffectView*) v;
      description = [NSString stringWithFormat: @"%@ state=%ld", description, (long) vv.state];
    }
    return description;
  } else {
    return @"";
  }
}

static void viewDebug(NSView *v, NSString *title, int indent) {
  NSString *titleString = title ? [NSString stringWithFormat: @"%@: ", title] : @"";
  NSString *od = v.opaque ? @" Opaque" : @"";
  NSString *viewDescription = createViewDescription(v);

  NSLog(@"%@%@%@%@ %f %f %f %f",
    createIndentation(indent),
    titleString, viewDescription, od,
    v.frame.origin.x, v.frame.origin.y, v.bounds.size.width, v.bounds.size.height);

  for (NSView *sv in v.subviews) {
    viewDebug(sv, @"", indent+2);
  }
}

static NSView *getTopView(NSWindow *w) {
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

static void windowDebug(NSWindow *w) {
  NSString *od = w.opaque ? @" Opaque" : @"";
  NSRect frame = w.frame;
  NSLog(@"Window: %@ %@%@ %f %f %f %f", w.title, [w description], od,
    frame.origin.x, frame.origin.y, frame.size.width, frame.size.height);
  NSView *v = getTopView(w);
  if (v != nil) {
    viewDebug(v, @"", 2);
  }
}

//
// FakeParentWindow - allow control over the active/inactive status
//

@interface FakeParentWindow : NSWindow
@end

@implementation FakeParentWindow
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

// Support for views that display differently in toolbars

@interface NSView (NSViewPrivate)
- (int)_semanticContext;
@end

@interface MyFakeToolbarContainer : NSView
- (int)_semanticContext;
@end

@implementation MyFakeToolbarContainer
- (int)_semanticContext
{
  return 5;
}
@end

static FakeParentWindow *fakeParentWindow;
static FakeParentWindow *fakeTexturedWindow;
static FakeParentWindow *fakeDocumentWindow;
static MyPanel *myPanel;
static MyFakeToolbarContainer *fakeToolbarContainer;

static void init()
{
  if (!fakeParentWindow) {
    NSRect rect = NSMakeRect(0, 0, 10000, 10000);
    fakeParentWindow = [[FakeParentWindow alloc] initWithContentRect: rect
      styleMask: NSBorderlessWindowMask
      backing: NSBackingStoreNonretained
      defer: YES
      ];
  }

  // A textured window background is needed for textured separated segmented controls and
  // for inactive textured segmented controls.
  if (!fakeTexturedWindow) {
    NSRect rect = NSMakeRect(0, 0, 10000, 10000);
    fakeTexturedWindow = [[FakeParentWindow alloc] initWithContentRect: rect
      styleMask: NSTexturedBackgroundWindowMask
      backing: NSBackingStoreNonretained
      defer: YES
      ];
  }

  if (!fakeDocumentWindow) {
    NSRect rect = NSMakeRect(0, 0, 10000, 10000);
    fakeDocumentWindow = [[FakeParentWindow alloc] initWithContentRect: rect
      styleMask: (NSTitledWindowMask|NSClosableWindowMask|NSMiniaturizableWindowMask|NSResizableWindowMask)
      backing: NSBackingStoreNonretained
      defer: YES
      ];
  }

  if (!myPanel) {
    NSRect rect = NSMakeRect(0, 0, 10000, 10000);
    myPanel = [[MyPanel alloc] initWithContentRect: rect
      styleMask: (NSUtilityWindowMask|NSTitledWindowMask|NSClosableWindowMask|NSMiniaturizableWindowMask|NSResizableWindowMask)
      backing: NSBackingStoreNonretained
      defer: YES
      ];
  }

  if (!fakeToolbarContainer) {
    NSRect rect = NSMakeRect(0, 0, 9000, 9000);
    fakeToolbarContainer = [[MyFakeToolbarContainer alloc] initWithFrame: rect];
  }
}

// Support for views that display differently in toolbars

static void installContentViewInWindow(FakeParentWindow *window, NSView *view, BOOL inToolbar)
{
  if (inToolbar) {
    [fakeToolbarContainer addSubview: view];
    window.contentView = fakeToolbarContainer;
  } else {
    window.contentView = view;
  }
}

static void installContentView(NSView *view, BOOL inToolbar)
{
  installContentViewInWindow(fakeParentWindow, view, inToolbar);
}

@interface FakeButton : NSButton
@end

@implementation FakeButton
//- (BOOL) canDraw
//{
//	return YES;
//}
@end

static NSGraphicsContext *setupRaw(int *data, int rw, int rh, int w, int h)
{
  // The NSBitmapImageRep approach creates an immutable raster unless you let it allocate
  // its own memory.

  //rasterBuffer = (unsigned char *) data;

// 	NSBitmapImageRep *bmpImageRep = [[NSBitmapImageRep alloc]
// 									  initWithBitmapDataPlanes:NULL
// 									  pixelsWide:rw
// 									  pixelsHigh:rh
// 									  bitsPerSample:8
// 									  samplesPerPixel:4
// 									  hasAlpha:YES
// 									  isPlanar:NO
// 									  colorSpaceName:NSCalibratedRGBColorSpace
// 									  bitmapFormat:NSAlphaFirstBitmapFormat
// 									  bytesPerRow:0
// 									  bitsPerPixel:0
// 									  ];
// 	// There isn't a colorspace name constant for sRGB so retag
// 	// using the sRGBColorSpace method
// 	bmpImageRep = [bmpImageRep bitmapImageRepByRetaggingWithColorSpace: [NSColorSpace sRGBColorSpace]];
// 	// Setting the user size communicates the dpi
// 	[bmpImageRep setSize:NSMakeSize(w, h)];
// 	// Create a bitmap context
// 	currentGraphicsContext = [NSGraphicsContext graphicsContextWithBitmapImageRep:bmpImageRep];

  CGColorSpaceRef colorspace = CGColorSpaceCreateDeviceRGB();
  currentCGContext = CGBitmapContextCreate(data, rw, rh, 8, rw * 4, colorspace, kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Host);
  CGColorSpaceRelease(colorspace);

  float xscale = ((float) rw) / w;
  float yscale = ((float) rh) / h;
  CGContextScaleCTM(currentCGContext, xscale, yscale);

  // Issue: the following method is deprecated in OS 10.10
  // instead can use the equivalent graphicsContextWithCGContext:flipped: introduced in 10.10

  currentGraphicsContext = [NSGraphicsContext graphicsContextWithGraphicsPort:currentCGContext flipped:NO];

  [NSGraphicsContext saveGraphicsState];
  [NSGraphicsContext setCurrentContext: currentGraphicsContext];
  init();
  NSRect frameRect = NSMakeRect(0, 0, w, h);
  [fakeParentWindow setFrame: frameRect display: NO];
  [fakeTexturedWindow setFrame: frameRect display: NO];
  return currentGraphicsContext;
}

static NSGraphicsContext *setup(JNIEnv *env, jintArray data, jint rw, jint rh, jfloat w, jfloat h)
{
  pixelData = data;
  jboolean isCopy = JNI_FALSE;
  rawPixelData = (*env)->GetPrimitiveArrayCritical(env, data, &isCopy);
  if (rawPixelData) {
    return setupRaw(rawPixelData, rw, rh, w, h);
  } else {
    return NULL;
  }
}

static void cleanupGraphics(JNIEnv *env)
{
  if (rawPixelData) {
    (*env)->ReleasePrimitiveArrayCritical(env, pixelData, rawPixelData, 0);
    rawPixelData = NULL;
  }

  pixelData = NULL;

  if (currentGraphicsContext) {
    [NSGraphicsContext restoreGraphicsState];
    currentGraphicsContext = NULL;
  }

  if (currentCGContext) {
    CGContextRelease(currentCGContext);
    currentCGContext = NULL;
  }
}

static void cleanup(JNIEnv *env)
{
  cleanupGraphics(env);

  if (fakeParentWindow) {
    [fakeParentWindow setContentView: NULL];
  }

  if (fakeTexturedWindow) {
    [fakeTexturedWindow setContentView: NULL];
  }

  if (fakeToolbarContainer) {
    for (NSView *sv in fakeToolbarContainer.subviews) {
      [sv removeFromSuperview];
    }
  }
}

static void setControlSize(NSView* v, int sz)
{
  // Large not supported

  NSControlSize size = NSRegularControlSize;

  switch (sz)
  {
    case MiniSize:
      size = NSMiniControlSize;
      break;
    case SmallSize:
      size = NSSmallControlSize;
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

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativePaintIndeterminateProgressBar
 * Signature: ([IIIIIIIIZI)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintIndeterminateProgressIndicator
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h, jint sz, jint st, jint o, jboolean isSpinner, jint frame)
{
  COCOA_ENTER(env);
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
    if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSProgressIndicator* view = [[NSProgressIndicator alloc] initWithFrame: frameRect];
    if (isSpinner) {
      [view setStyle: NSProgressIndicatorSpinningStyle];
    } else {
      [view setIndeterminate: YES];
    }
    [fakeParentWindow setContentView: view];
    setControlSize(view, sz);
    setControlState(view, st);
    [view displayRectIgnoringOpacity: frameRect inContext: gc];

    // A crude simulation of animation frames

    if (frame > 0) {
      frame = frame % 10;
      NSRectClip(NSMakeRect(5, 0, w-10, h));
      NSAffineTransform* xform = [NSAffineTransform transform];
      [xform translateXBy: -w + 40 + frame * w/15 yBy: 0];
      [xform concat];
      frameRect = NSMakeRect(0, 0, 2*w, h);
      [view setFrame: frameRect];
      [fakeParentWindow setFrame: frameRect display: NO];
      [view displayRectIgnoringOpacity: frameRect inContext: gc];
    }

    cleanup(env);
    }
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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
    if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSProgressIndicator* view = [[NSProgressIndicator alloc] initWithFrame: frameRect];
    [fakeParentWindow setContentView: view];
    setControlSize(view, sz);
    setControlState(view, st);
    [view setIndeterminate: NO];
    [view setMaxValue: 1];
    [view setDoubleValue: v];
    [view displayRectIgnoringOpacity: frameRect inContext: gc];

    cleanup(env);
    }
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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {

    NSRect frameRect = NSMakeRect(0, 0, w, h);

    if (buttonView == NULL) {
      buttonView = [[FakeButton alloc] initWithFrame: frameRect];
    } else {
      [buttonView setFrame: frameRect];
    }

    BOOL isHighlight = NO;
    BOOL allowsMixedState = NO;
    BOOL shouldPaint = YES;
    BOOL inToolbar = NO;

    if (bezelStyle >= 1000) {
      inToolbar = YES;
      bezelStyle -= 1000;
    }

    installContentView(buttonView, inToolbar);

    if (value == 2) {
      allowsMixedState = YES;
      value = -1;
    } else if (value == 1) {
      if (bezelStyle != NSRoundedBezelStyle && bezelStyle != NSHelpButtonBezelStyle
        && buttonType != NSSwitchButton && buttonType != NSRadioButton && buttonType != NSPushOnPushOffButton) {
        //isHighlight = YES;
        //value = 0;
        buttonType = NSPushOnPushOffButton;
      }
    } else {
      value = 0;
    }

    [fakeParentWindow setDefaultButtonCell: NULL];

    if (st == DefaultState) {

      [fakeParentWindow setDefaultButtonCell: [buttonView cell]];

    } else if (st == PressedState) {

      isHighlight = YES;

    }

    if (shouldPaint) {
      setControlSize(buttonView, sz);
      setControlState(buttonView, st);
      [buttonView setButtonType: buttonType];
      [buttonView setBezelStyle: bezelStyle];
      [buttonView setAllowsMixedState: allowsMixedState];
      [buttonView setState: value];
      [buttonView highlight: isHighlight];
      [buttonView setTitle: @""];
      [[buttonView cell] setUserInterfaceLayoutDirection: layoutDirection];
      [buttonView displayRectIgnoringOpacity: frameRect inContext: gc];
    }

    // TBD: Is this an AppKit bug? Once set to NSSwitchButton or NSBRadioButton,
    // further changes have no effect.
    if (buttonType == NSSwitchButton || buttonType == NSRadioButton) {
      buttonView = NULL;
    }

    cleanup(env);
  }
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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
    if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSColorWell *view = [[NSColorWell alloc] initWithFrame: frameRect];
    [fakeParentWindow setContentView: view];
    setControlState(view, st);
    [view setIntegerValue: st == PressedState];		// does not work
    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
    }
  COCOA_EXIT(env);
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
 * Signature: ([IIIIIIIIIIZI)V
 */
JNIEXPORT void JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativePaintSegmentedButton
  (JNIEnv *env, jclass cl, jintArray data, jint rw, jint rh, jfloat w, jfloat h,
    jint segmentStyle, jint segmentPosition, jint sz, jint st, jboolean isFocused, jint flags,
    jfloatArray debugOutput, jintArray debugData)
{
  COCOA_ENTER(env);

  FakeParentWindow *window = fakeParentWindow;

  //NSLog(@"Segmented button %d", segmentStyle);  // debug

  BOOL is1x = rw == w;
  BOOL isElCap = floor(NSAppKitVersionNumber) > NSAppKitVersionNumber10_10_Max;
  BOOL isLeft = segmentPosition == SEGMENT_POSITION_ONLY || segmentPosition == SEGMENT_POSITION_FIRST;

  // The following layout parameters are hand tuned and must be kept in sync with the actual painting. They describe
  // empty space on either end of the segmented control relative to the control frame.

  float dividerWidth = 1;		// width of the dividers
  float outerLeftInset = 0;	// width of empty space on the left side of the control
  float leftInset = 3;      // extra space on the left side of the control that is not part of a segment
  float rightInset = 3;     // extra space on the right side of the control that is not part of a segment
  float topInset = 0;

  if (isElCap && !is1x) {
    outerLeftInset = 0.5f;
  }

  BOOL inToolbar = NO;
  if (segmentStyle >= 1000) {
    inToolbar = YES;
    segmentStyle -= 1000;
  }

  jint originalSegmentStyle = segmentStyle;

  if (segmentStyle == NSSegmentStyleSeparated_Rounded) {
    segmentStyle = NSSegmentStyleSeparated;
  } else if (segmentStyle == NSSegmentStyleSeparated_Textured) {
    segmentStyle = NSSegmentStyleSeparated;
    window = fakeTexturedWindow;
  } else if (segmentStyle == NSSegmentStyleTexturedSquare) {
    window = fakeTexturedWindow;
  }

  //NSLog(@" displayed style %d", segmentStyle);  // debug
  //NSLog(@" state %d, flags %d", st, flags);  // debug
  //NSLog(@" requested width %f", w); // debug

  if (segmentStyle == NSSegmentStyleRounded || originalSegmentStyle == NSSegmentStyleSeparated_Rounded) {
    // Push button and tab style
    switch (sz) {
      case RegularSize: leftInset = is1x ? 1 : 1.5f; rightInset = is1x ? 1 : 0.5f; outerLeftInset = 2; break;
      case SmallSize:   leftInset = is1x ? 1 : 1.5f; rightInset = is1x ? 1 : 0.5f; outerLeftInset = 2; topInset = -0.49f; break;
      case MiniSize:    leftInset = is1x ? 2 : 2.5f; rightInset = is1x ? 2 : 1.5f; outerLeftInset = 1; break;
    }
    if (isElCap && !is1x) {
      leftInset += 0.5f;
      outerLeftInset -= 0.5f;
    }
  } else if (segmentStyle == NSSegmentStyleRoundRect) {
    // Inset style
    leftInset = is1x ? 2 : 2.5f;
    rightInset = is1x ? 2 : 1.5f;
    outerLeftInset = 1;
    if (isElCap && !is1x) {
      leftInset += 0.5f;
      outerLeftInset -= 0.5f;
    }
  } else if (segmentStyle == NSSegmentStyleCapsule) {
    // S-Curve style (like textured except for layout)
    leftInset = is1x ? 3 : 3.5f;
    rightInset = is1x ? 3 : 2.5f;
    if (isElCap && !is1x) {
      leftInset += 0.5f;
      rightInset -= 0.5f;
      outerLeftInset -= 0.5f;
    }
  } else if (segmentStyle == NSSegmentStyleTexturedRounded) {
    // Rounded textured style (aka toolbar)
    if (sz == RegularSize) {
      leftInset = is1x ? 3 : 3.5f;
      rightInset = is1x ? 3 : 2.5f;
    } else {
      leftInset = is1x ? 1 : 1.5f;
      rightInset = is1x ? 1 : 0.5f;
    }
    if (isElCap && !is1x) {
      leftInset += 0.5f;
      rightInset -= 0.5f;
      outerLeftInset -= 0.5f;
    }
  } else if (segmentStyle == NSSegmentStyleTexturedSquare) {
    // Normal textured style
    if (isElCap) {
      leftInset = is1x ? 1 : inToolbar ? 1 : 2;
      rightInset = is1x ? 1 : 0;
      if (!inToolbar) {
        // The 0.5 point outer left inset does not work on El Capitan if the control is not in a toolbar.
        outerLeftInset = 0;
      }
    } else {
      leftInset = is1x ? 1 : 1.5f;
      rightInset = is1x ? 1 : 0.5f;
    }
  } else if (originalSegmentStyle == NSSegmentStyleSeparated_Textured) {
    // Separated textured style
    if (sz == RegularSize) {
      leftInset = is1x ? 3 : 3.5f;
      rightInset = is1x ? 3 : 2.5f;
    } else {
      leftInset = is1x ? 1 : 1.5f;
      rightInset = is1x ? 1 : 0.5f;
    }
    if (isElCap && !is1x) {
      leftInset -= 0.5f;
      rightInset -= 1;
    }
  } else if (segmentStyle == NSSegmentStyleSmallSquare) {
    // Square style
    outerLeftInset = 0;
    leftInset = rightInset = 1;
  }

  // A segmented control with one segment is painted directly.

  // Otherwise, we create a segmented control large enough for four segments and render it into the buffer.
  // The four segments correspond to the three possible position based renderings with an option for
  // whether the middle segment is next to a selected segment or not.
  // We set the user space of the graphics context so that we capture the appropriate region in the buffer.

  // The segment width will be less than the provided width because the provided width includes
  // space for a border and/or a divider.

  // All the dividers will be painted, but only the desired ones are included in the requested bounds.

  // Right to left orientation is not supported. Not sure it needs to be here.

  float otherSegmentWidth = 40;

  float segmentWidth;
  int segmentIndex;
  int selectedSegmentIndex = -1;

  float xOffset = 0;
  float widthAdjustment = 0;

  float cw;

  if (segmentPosition == SEGMENT_POSITION_ONLY) {
    cw = w + outerLeftInset;
  } else {
    if (segmentPosition == SEGMENT_POSITION_FIRST) {
      segmentIndex = 0;
      segmentWidth = w - leftInset;
    } else if (segmentPosition == SEGMENT_POSITION_MIDDLE) {
      segmentIndex = 1;
      segmentWidth = w;
    } else if (segmentPosition == SEGMENT_POSITION_LAST) {
      segmentIndex = 3;
      segmentWidth = w - rightInset;
    } else {
      // should not happen
      @throw([NSException exceptionWithName: NSInvalidArgumentException reason: @"Invalid segment position parameter" userInfo: nil]);
    }

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
      // the width of the left segment is otherSegmentWidth + leftInset
      xOffset = leftInset + segmentIndex * (otherSegmentWidth + dividerWidth);
      widthAdjustment += leftInset;
    }

    if (segmentIndex < 3) {
      // the width of the right segment is otherSegmentWidth + rightInset
      widthAdjustment += rightInset;
    }

    if (segmentIndex > 0 && (flags & SEGMENT_FLAG_DRAW_LEADING_SEPARATOR) != 0) {
      xOffset -= dividerWidth;
      segmentWidth -= dividerWidth;
      widthAdjustment -= dividerWidth;
    }

    if (segmentIndex < 3 && (flags & SEGMENT_FLAG_DRAW_TRAILING_SEPARATOR) != 0) {
      segmentWidth -= dividerWidth;
      widthAdjustment -= dividerWidth;
    }

    cw = outerLeftInset + w + 3 * (dividerWidth + otherSegmentWidth) + widthAdjustment;
  }

  float ch = h + topInset;

  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {

    NSRect controlFrame = NSMakeRect(0, 0, cw, ch);
    [window setFrame: controlFrame display: NO];

    NSSegmentedControl *view;

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

      segmentWidth = w - (leftInset + rightInset);

      [view setWidth: segmentWidth forSegment: 0];
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
    installContentViewInWindow(window, view, inToolbar);
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

    NSAffineTransform* xform = [NSAffineTransform transform];
    [xform translateXBy: -(outerLeftInset + xOffset) yBy: topInset];
    [xform concat];
    [view displayRectIgnoringOpacity: controlFrame inContext: currentGraphicsContext];

    // If requested, return an image of the entire control.

    int crw = 0;    // raster width in pixels
    int crh = 0;    // raster height in pixels

    if (debugData) {
      cleanupGraphics(env);

      cw = ceil(cw);
      ch = ceil(ch);

      float xScale = rw / w;
      float yScale = rh / h;
      crw = (int) (xScale * cw);
      crh = (int) (yScale * ch);

      //NSLog(@"Creating debug image %d %d", crw, crh);

      NSGraphicsContext *gc = setup(env, debugData, crw, crh, cw, ch);
      if (gc) {
        [view displayRectIgnoringOpacity: controlFrame inContext: currentGraphicsContext];
      }
    }

    if (debugOutput) {
      jfloat *a = (*env)->GetFloatArrayElements(env, debugOutput, NULL);
      if (a) {
        a[DEBUG_SEGMENT_WIDTH] = crw;
        a[DEBUG_SEGMENT_HEIGHT] = crh;
        a[DEBUG_SEGMENT_X_OFFSET] = outerLeftInset + xOffset;
        a[DEBUG_SEGMENT_Y_OFFSET] = topInset;
        a[DEBUG_SEGMENT_DIVIDER_WIDTH] = dividerWidth;
        a[DEBUG_SEGMENT_OUTER_LEFT_INSET] = outerLeftInset;
        a[DEBUG_SEGMENT_LEFT_INSET] = leftInset;
        a[DEBUG_SEGMENT_RIGHT_INSET] = rightInset;
        (*env)->ReleaseFloatArrayElements(env, debugOutput, a, 0);
      }
    }

    segmentedControl = nil;
    segmentedControl4 = nil;

    cleanup(env);
  }
  COCOA_EXIT(env);
}

/*
 * Class:     org_violetlib_jnr_aqua_impl_AquaNativePainter
 * Method:    nativeDetermineSegmentedButtonFixedHeight
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_violetlib_jnr_aqua_impl_AquaNativePainter_nativeDetermineSegmentedButtonFixedHeight
  (JNIEnv *env, jclass cl, jint segmentStyle, jint sz)
{
  jint result = -1;

    COCOA_ENTER(env);

  init();

  float originalWidth = 1000;
  float originalHeight = 1000;
  NSRect frameRect = NSMakeRect(0, 0, originalWidth, originalHeight);
  NSSegmentedControl* view = [[NSSegmentedControl alloc] initWithFrame: frameRect];

  [view setSegmentStyle: segmentStyle];
  [view setSegmentCount: 1];
  setControlSize(view, sz);
  [view setLabel: @"Text" forSegment: 0];
  [view sizeToFit];
  result = [view bounds].size.height;		// all segmented controls are fixed height

  // For unknown reasons, this height is too small!

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
  jint result = -1;

    COCOA_ENTER(env);

  // Test the button using two different titles.
  // If the height is the same both times, then the height must be fixed.

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
  jint result = -1;

    COCOA_ENTER(env);

  // I do not know how to do an unbiased test, since any button that displays a title
  // will probably change its width based on the title. But we do not use a title, so for
  // us some buttons have a fixed width.

  // This test uses knowledge of the platform that certain types of buttons have a
  // fixed width when no title is used.

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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {

    // TBD: pressed and inactive states are not drawn correctly

    BOOL inToolbar = NO;

    if (bezelStyle >= 1000) {
      inToolbar = YES;
      bezelStyle -= 1000;
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

    if (bezelStyle == NSTexturedRoundedBezelStyle) {
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
    //[buttonView highlight: st == PressedState];			// does not work
    //[buttonView setIntegerValue: st == PressedState];		// does not work
    if (type > 0) {
      // INDICATOR_ONLY or ARROWS_ONLY
      NSUserInterfaceLayoutDirection dir = [view userInterfaceLayoutDirection];
      NSRectClip(dir == NSUserInterfaceLayoutDirectionLeftToRight ? NSMakeRect(w - indicatorWidth, 0, indicatorWidth, h) : NSMakeRect(0, 0, indicatorWidth, h));
    }
    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
  }
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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSPopUpButton* view = [[NSPopUpButton alloc] initWithFrame: frameRect pullsDown: !isUp];

    BOOL inToolbar = NO;

    if (bezelStyle >= 1000) {
      inToolbar = YES;
      bezelStyle -= 1000;
    }

    installContentView(view, inToolbar);

    // bezelStyle 0 is for cells (borderless)
    // A recessed button border should be drawn only in the rollover state.
    // Instead, a darker border is painted in all states.

    jboolean isBordered = !(bezelStyle == 0 || (bezelStyle == NSRecessedBezelStyle && st != RolloverState));
    if (bezelStyle == 0) {
      bezelStyle = NSShadowlessSquareBezelStyle;
      // mini arrows not supported separately
      if (sz == MiniSize) {
        sz = SmallSize;
      }
    }

    jint buttonType = NSMomentaryPushInButton;
    if (bezelStyle == NSRoundedBezelStyle) {
      buttonType = NSMomentaryLightButton;
    } else if (buttonType == NSRecessedBezelStyle) {
      buttonType = NSPushOnPushOffButton;
    }

    setControlSize(view, sz);
    setControlState(view, st);
    [[view cell] setBordered: isBordered];
    [[view cell] setUserInterfaceLayoutDirection: layoutDirection];

    //[view highlight: st == PressedState];			// does not work
    //[view setIntegerValue: st == PressedState];	// does not work

    [view setBezelStyle: bezelStyle];
    [view setButtonType: buttonType];
    [[view cell] setArrowPosition: NSPopUpArrowAtBottom];

    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
    }
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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
    if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSBox* view = [[NSBox alloc] initWithFrame: frameRect];
    [fakeParentWindow setContentView: view];

    setControlState(view, state);
    [view setTitlePosition: titlePosition];
    [view setTitle: @""];
    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
  }
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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSTextField* view;

    BOOL inToolbar = NO;
    if (type >= 1000) {
      inToolbar = YES;
      type -= 1000;
    }

    // In OS X 10.11 can do [searchField setCentersPlaceholder: NO];

    // TBD: The cancel button is too dark. Do not know why.

    if (type >= TextFieldSearch) {
      NSSearchField *searchField = [[MySearchField alloc] initWithFrame: frameRect];
      view = searchField;
      NSSearchFieldCell *cell = [searchField cell];

      if (type == TextFieldSearchWithCancel || type == TextFieldSearchWithMenuAndCancel) {
        [view setStringValue: @" "];	// The cancel button is drawn only if there is text

        //NSButtonCell *cbcell = [cell cancelButtonCell];
        //[cbcell setEnabled: NO];
      }

      if (type == TextFieldSearchWithMenu || type == TextFieldSearchWithMenuAndCancel) {
        // show the menu icon
        // TBD: this is not working
        // Probably need to field to be focused
        NSMenu *cellMenu = [[NSMenu alloc] initWithTitle: @"Dummy"];
        NSMenuItem *item = [[NSMenuItem alloc] initWithTitle:@"Clear" action:NULL keyEquivalent:@""];
        [item setTag:NSSearchFieldClearRecentsMenuItemTag];
        [cellMenu insertItem:item atIndex:0];
        [cell setSearchMenuTemplate:cellMenu];
      }

    } else {
      view = [[NSTextField alloc] initWithFrame: frameRect];
      if (type == TextFieldNormal) {
        [view setBezelStyle: NSTextFieldSquareBezel];
      } else if (type == TextFieldRound) {
        [view setBezelStyle: NSTextFieldRoundedBezel];
      }
    }

    installContentView(view, inToolbar);

    setControlSize(view, sz);
    setControlState(view, state);

    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
  }
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

  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
    if (gc) {

    // Create a table view large enough to display the header cell and render it into the buffer.
    // Note that a header is displayed only if the table is contained in a scroll view.
    // We need to know where to find the header within the table view.

    // The design requires that cell dividers on both sides be rendered into the buffer.
    // It does not allow a top border to be rendered into the buffer.

    float leftInset = 12;	// prevent the fake first column from being rendered into the buffer
    float topInset = 4.49;	// prevent the top border from being rendered into the buffer
    int cellWidth = w - 4;

    int cw = w + 1 + 20;
    int ch = h + 9;
    NSRect controlFrame = NSMakeRect(0, 0, cw, ch);
    [fakeParentWindow setFrame: controlFrame display: NO];

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

    [cell setUserInterfaceLayoutDirection: layoutDirection];	// not working

    NSView *view = [table headerView];

    [fakeParentWindow setContentView: view];

    // Set the user space so that the header cell is rendered in the top left corner
    // The bottom-up coordinate system makes this code confusing...
    NSAffineTransform* xform = [NSAffineTransform transform];
    [xform translateXBy: -leftInset yBy: h - ch + topInset];
    [xform concat];

    [view displayRectIgnoringOpacity: controlFrame inContext: currentGraphicsContext];

    cleanup(env);
    }

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
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
    if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSSlider* view = [[NSSlider alloc] initWithFrame: frameRect];
    [fakeParentWindow setContentView: view];

    setupSlider(view, w, h, sliderType, sz, state, numberOfTickMarks, tickMarkPosition, value);

    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
  }
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

  int *data = calloc(w * h, sizeof(int));
  NSGraphicsContext *gc = setupRaw(data, w, h, w, h);
  if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSSlider* view = [[NSSlider alloc] initWithFrame: frameRect];
    [fakeParentWindow setContentView: view];

    ThumbCapturingSliderCell *cell = [[ThumbCapturingSliderCell alloc] init];
    [view setCell: cell];

    setupSlider(view, w, h, sliderType, sz, ActiveState, numberOfTickMarks, tickMarkPosition, value);

    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    NSRect thumbBounds = [cell getCapturedBounds];

    cleanup(env);

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
  }
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

  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSStepper* view = [[NSStepper alloc] initWithFrame: frameRect];
    [fakeParentWindow setContentView: view];

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

    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
  }

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

  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSSplitView *splitView = [[NSSplitView alloc] initWithFrame: frameRect];
    NSRect subviewFrame = NSMakeRect(0, 0, w, h);
    NSView *view1 = [[NSView alloc] initWithFrame: subviewFrame];
    NSView *view2 = [[NSView alloc] initWithFrame: subviewFrame];
    [splitView addSubview:view1];
    [splitView addSubview:view2];
    [splitView setPosition: 0 ofDividerAtIndex: 0];

    [fakeParentWindow setContentView: splitView];

    setControlState(splitView, state);
    [splitView setDividerStyle: type];
    [splitView setVertical: o];

    [splitView displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
  }

  COCOA_EXIT(env);
}

@interface NSWindow (NSWindowPrivate)
- (NSView *)_borderView;
@end

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

  init();

  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    [fakeParentWindow setContentView: NULL];

    isActive = state == ActiveState;	// only Active and Inactive are supported

    NSWindow *w = type == 0 ? fakeDocumentWindow : myPanel;
    [w setFrame: frameRect display:NO];
    [w setDocumentEdited: isDirty];

    // Surprisingly, when highlighted, the buttons also paint the icons.

    NSWindowCollectionBehavior behavior = [w collectionBehavior];
    if (resizeIsFullScreen) {
      behavior |= NSWindowCollectionBehaviorFullScreenPrimary;
    } else {
      behavior &= ~NSWindowCollectionBehaviorFullScreenPrimary;
    }
    [w setCollectionBehavior:behavior];

    // We can force a button to display as inactive.

    NSButton *minimizeButton = [w standardWindowButton: NSWindowMiniaturizeButton];
    if (minimizeButton) {
      configureTitleBarButton(minimizeButton, minimizeState);
    }

    NSButton *resizeButton = [w standardWindowButton: NSWindowZoomButton];
    if (resizeButton) {
      configureTitleBarButton(resizeButton, resizeState);
      // Not clear it is possible to get the window to paint a full screen exit icon.
      // Therefore, we display no icon.
      [[resizeButton cell] setHighlighted: NO];
    }

    // Displaying the window does not work, but displaying the border view does.

    NSButton *closeButton = [w standardWindowButton: NSWindowCloseButton];
    if (closeButton) {
      configureTitleBarButton(closeButton, closeState);
      NSView *borderView = [closeButton superview];
      [borderView displayRectIgnoringOpacity: frameRect inContext: gc];
    }

    cleanup(env);
  }
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
  jintArray result = NULL;

  int rawData[12];

  COCOA_ENTER(env);

  init();

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

  if (0) {
    // Testing
    float width1 = [NSScroller scrollerWidthForControlSize: NSRegularControlSize scrollerStyle: NSScrollerStyleOverlay];
    float width2 = [NSScroller scrollerWidthForControlSize: NSSmallControlSize scrollerStyle: NSScrollerStyleOverlay];
    float width3 = [NSScroller scrollerWidthForControlSize: NSMiniControlSize scrollerStyle: NSScrollerStyleOverlay];
    float width4 = [NSScroller scrollerWidthForControlSize: NSRegularControlSize scrollerStyle: NSScrollerStyleLegacy];
    float width5 = [NSScroller scrollerWidthForControlSize: NSSmallControlSize scrollerStyle: NSScrollerStyleLegacy];
    float width6 = [NSScroller scrollerWidthForControlSize: NSMiniControlSize scrollerStyle: NSScrollerStyleLegacy];
    NSLog(@"scroller width = %f %f %f %f %f %f", width1, width2, width3, width4, width5, width6);
  }

  // Only Regular and Small sizes are supported

  COCOA_ENTER(env);
  NSGraphicsContext *gc = setup(env, data, rw, rh, w, h);
  if (gc) {
    NSRect frameRect = NSMakeRect(0, 0, w, h);
    NSScroller* view = [[NSScroller alloc] initWithFrame: frameRect];
    [fakeParentWindow setContentView: view];

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
    [view displayRectIgnoringOpacity: frameRect inContext: gc];
    cleanup(env);
  }
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

  if (0) {
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
