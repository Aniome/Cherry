package com.app.cherry.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UniqueElementCodeArea {
    boolean isMarked;
    String text;
    int lineNumber;
}
