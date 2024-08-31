package com.vexsoftware.nuvotifier.standalone.receiver;

import com.vexsoftware.votifier.model.Vote;

public interface VoteReceiver {

    void onVote(Vote vote) throws Exception;

    void onException(Throwable throwable);
}
