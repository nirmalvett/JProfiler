package com.vettiankal;

import java.util.HashMap;

public interface ProfileCompleteEvent {

    void onComplete(HashMap<ThreadInfo, ExecutionTree> tree);

}
