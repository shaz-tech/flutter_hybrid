//
//  UIViewController+Utility.h
//  Runner
//
//  Created by JianFei Wang on 2019/8/28.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (Utility)

+ (UIViewController *)rootViewController;
+ (UIViewController *)topViewController;
+ (nullable UINavigationController *)currentNavigationController;

@end

NS_ASSUME_NONNULL_END
