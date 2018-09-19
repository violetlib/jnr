/*
 * Copyright (c) 2018 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

#import <Cocoa/Cocoa.h>
#include "AppearanceSupport.h"

static NSMutableArray *knownAppearanceNames;

NSAppearance *configuredAppearance;

NSUInteger registerAppearance(NSString *appearanceName)
{
    if (!knownAppearanceNames) {
        knownAppearanceNames = [[NSMutableArray arrayWithCapacity:8] retain];
    }

    NSUInteger index = [knownAppearanceNames indexOfObject:appearanceName];
    if (index != NSNotFound) {
        return index;
    }

    [knownAppearanceNames addObject:[appearanceName retain]];
    return knownAppearanceNames.count - 1;
}

void setAppearance(NSUInteger appearanceID) {

    if (knownAppearanceNames && appearanceID >= 0 && appearanceID < knownAppearanceNames.count) {
        NSString *appearanceName = (NSString *) knownAppearanceNames[appearanceID];
        configuredAppearance = [NSAppearance appearanceNamed:appearanceName];
    } else {
        NSLog(@"Invalid appearance ID: %ld", (long) appearanceID);
        configuredAppearance = [NSAppearance appearanceNamed:NSAppearanceNameAqua];
    }
}
