package mekhq.campaign.market;

import mekhq.campaign.Campaign;
import mekhq.campaign.mission.AtBContract;

public class DisabledContractMarket extends AbstractContractMarket {

    @Override
    public AtBContract addAtBContract(Campaign campaign) {
        return null;
    }

    @Override
    public void generateContractOffers(Campaign campaign, boolean newCampaign) {

    }

    @Override
    public void addFollowup(Campaign campaign, AtBContract contract) {

    }

    @Override
    protected void setAtBContractClauses(AtBContract contract, int unitRatingMod, Campaign campaign) {

    }
}
