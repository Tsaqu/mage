
package mage.abilities.keyword;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.*;
import mage.cards.Card;
import mage.cards.SplitCardHalf;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.stack.Spell;
import mage.players.Player;

/**
 * Aftermath
 *
 * TODO: Implement once we get details on the comprehensive rules meaning of the
 * ability
 *
 * Current text is a shell copied from Flashback
 *
 * @author stravant
 */
public class AftermathAbility extends SimpleStaticAbility {

    public AftermathAbility() {
        super(Zone.ALL, new AftermathCastFromGraveyard());
        addEffect(new AftermathCantCastFromHand());
        addEffect(new AftermathExileAsResolvesFromGraveyard());
    }

    public AftermathAbility(final AftermathAbility ability) {
        super(ability);
    }

    @Override
    public AftermathAbility copy() {
        return new AftermathAbility(this);
    }

    @Override
    public String getRule() {
        return "Aftermath <i>(Cast this spell only from your graveyard. Then exile it.)</i>";
    }
}

class AftermathCastFromGraveyard extends AsThoughEffectImpl {

    public AftermathCastFromGraveyard() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.EndOfGame, Outcome.Benefit);
    }

    public AftermathCastFromGraveyard(final AftermathCastFromGraveyard effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public AftermathCastFromGraveyard copy() {
        return new AftermathCastFromGraveyard(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        if (objectId.equals(source.getSourceId())
                && affectedControllerId.equals(source.getControllerId())) {
            Card card = game.getCard(source.getSourceId());
            if (card != null && game.getState().getZone(source.getSourceId()) == Zone.GRAVEYARD) {
                return true;
            }
        }
        return false;
    }
}

class AftermathCantCastFromHand extends ContinuousRuleModifyingEffectImpl {

    public AftermathCantCastFromHand() {
        super(Duration.EndOfGame, Outcome.Detriment);
        staticText = ", but not from anywhere else";
    }

    public AftermathCantCastFromHand(final AftermathCantCastFromHand effect) {
        super(effect);
    }

    @Override
    public AftermathCantCastFromHand copy() {
        return new AftermathCantCastFromHand(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CAST_SPELL;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Card card = game.getCard(event.getSourceId());
        if (card != null && card.getId().equals(source.getSourceId())) {
            Zone zone = game.getState().getZone(card.getId());
            if (zone != null && (zone != Zone.GRAVEYARD)) {
                return true;
            }
        }
        return false;
    }
}

class AftermathExileAsResolvesFromGraveyard extends ReplacementEffectImpl {

    AftermathExileAsResolvesFromGraveyard() {
        super(Duration.WhileOnStack, Outcome.Detriment);
        this.staticText = "Exile it afterwards.";
    }

    AftermathExileAsResolvesFromGraveyard(AftermathExileAsResolvesFromGraveyard effect) {
        super(effect);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent evt, Ability source, Game game) {
        ZoneChangeEvent event = (ZoneChangeEvent) evt;
        if (event.getFromZone() == Zone.STACK && event.getToZone() != Zone.EXILED) {
            // Moving something from stack to somewhere else

            // Get the source id, getting the whole split card's ID, because
            // that's the card that is changing zones in the event, but
            // source.getSourceId is only the split card half.
            // If branch so that we also support putting Aftermath on
            // non-split cards for... whatever reason, in case somebody
            // wants to do that in the future.
            UUID sourceId = source.getSourceId();
            Card sourceCard = game.getCard(source.getSourceId());
            if (sourceCard != null && sourceCard instanceof SplitCardHalf) {
                sourceCard = ((SplitCardHalf) sourceCard).getParentCard();
                sourceId = sourceCard.getId();
            }

            if (event.getTargetId().equals(sourceId)) {
                // Moving this spell from stack to yard
                Spell spell = game.getStack().getSpell(source.getSourceId());
                if (spell != null && spell.getFromZone() == Zone.GRAVEYARD) {
                    // And this spell was cast from the graveyard, so we need to exile it
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        UUID sourceId = source.getSourceId();
        Card sourceCard = game.getCard(source.getSourceId());
        if (sourceCard != null && sourceCard instanceof SplitCardHalf) {
            sourceCard = ((SplitCardHalf) sourceCard).getParentCard();
            sourceId = sourceCard.getId();
        }

        if (sourceCard != null) {
            Player player = game.getPlayer(sourceCard.getOwnerId());
            if (player != null) {
                return player.moveCardToExileWithInfo(sourceCard, null, "", sourceId, game, ((ZoneChangeEvent) event).getFromZone(), true);
            }
        }
        return false;
    }

    @Override
    public AftermathExileAsResolvesFromGraveyard copy() {
        return new AftermathExileAsResolvesFromGraveyard(this);
    }

}
