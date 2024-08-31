package com.vexsoftware.nuvotifier.standalone.receiver.impl;

import com.vexsoftware.nuvotifier.standalone.config.VotifierConfiguration;
import com.vexsoftware.nuvotifier.standalone.receiver.VoteReceiver;
import com.vexsoftware.votifier.model.Vote;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoteReceiverImpl implements VoteReceiver {

    private final Logger logger = LoggerFactory.getLogger(VoteReceiver.class);

    @Inject
    private VotifierConfiguration configuration;

    @Override
    public void onVote(Vote vote) throws Exception {
        if (configuration.isDebug()) {
            logger.debug("Received vote for username {} from service {} using IP address {}",
                    vote.getUsername(),
                    vote.getServiceName(),
                    vote.getAddress()
            );
        }
    }

    @Override
    public void onException(Throwable throwable) {
        logger.error("Exception occurred while handling vote", throwable);
    }
}
