/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ..AS IS.. AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.game.command.planes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.MainPhaseStackEmptyCondition;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.RollPlanarDieEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInHandEffect;
import mage.abilities.keyword.ReboundAbility;
import mage.cards.Card;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.common.FilterInstantOrSorceryCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.CardTypePredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetCardInLibrary;
import mage.watchers.common.PlanarRollWatcher;

/**
 *
 * @author spjspj
 */
public class TrailOfTheMageRingsPlane extends Plane {

    private static final FilterCard filter = new FilterCard("creature spells");

    static {
        filter.add(new CardTypePredicate(CardType.CREATURE));
    }

    public TrailOfTheMageRingsPlane() {
        this.setName("Plane - Trail of the Mage-Rings");
        this.setExpansionSetCodeForImage("PCA");

        // Instant and sorcery spells have rebound
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new TrailOfTheMageRingsReboundEffect());
        this.getAbilities().add(ability);

        // Active player can roll the planar die: Whenever you roll {CHAOS}, you may search your library for an instant or sorcery card, reveal it, put it into your hand, then shuffle your library
        Effect chaosEffect = new SearchLibraryPutInHandEffect(new TargetCardInLibrary(0, 1, new FilterInstantOrSorceryCard()), true, true);
        Target chaosTarget = null;

        List<Effect> chaosEffects = new ArrayList<Effect>();
        chaosEffects.add(chaosEffect);
        List<Target> chaosTargets = new ArrayList<Target>();
        chaosTargets.add(chaosTarget);

        ActivateIfConditionActivatedAbility chaosAbility = new ActivateIfConditionActivatedAbility(Zone.COMMAND, new RollPlanarDieEffect(chaosEffects, chaosTargets), new GenericManaCost(0), MainPhaseStackEmptyCondition.instance);
        chaosAbility.addWatcher(new PlanarRollWatcher());
        this.getAbilities().add(chaosAbility);
        chaosAbility.setMayActivate(TargetController.ANY);
        this.getAbilities().add(new SimpleStaticAbility(Zone.ALL, new PlanarDieRollCostIncreasingEffect(chaosAbility.getOriginalId())));
    }
}

class TrailOfTheMageRingsReboundEffect extends ContinuousEffectImpl {

    protected static final FilterCard filter = new FilterCard("Instant and sorcery spells");

    static {
        filter.add(Predicates.or(new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));
    }

    public TrailOfTheMageRingsReboundEffect() {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Instant and sorcery spells have rebound";
    }

    public TrailOfTheMageRingsReboundEffect(final TrailOfTheMageRingsReboundEffect effect) {
        super(effect);
    }

    @Override
    public TrailOfTheMageRingsReboundEffect copy() {
        return new TrailOfTheMageRingsReboundEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Plane cPlane = game.getState().getCurrentPlane();
        if (cPlane == null) {
            return false;
        }
        if (cPlane != null) {
            if (!cPlane.getName().equalsIgnoreCase("Plane - Trail of the Mage-Rings")) {
                return false;
            }
        }

        for (UUID playerId : game.getPlayers().keySet()) {
            Player player = game.getPlayer(playerId);
            if (player != null) {
                for (Card card : player.getHand().getCards(filter, game)) {
                    addReboundAbility(card, source, game);
                }
                for (Iterator<StackObject> iterator = game.getStack().iterator(); iterator.hasNext();) {
                    StackObject stackObject = iterator.next();
                    if (stackObject instanceof Spell && stackObject.getControllerId().equals(source.getControllerId())) {
                        Spell spell = (Spell) stackObject;
                        Card card = spell.getCard();
                        if (card != null) {
                            addReboundAbility(card, source, game);
                        }
                    }
                }
            }
        }
        return true;
    }

    private void addReboundAbility(Card card, Ability source, Game game) {
        if (filter.match(card, game)) {
            boolean found = card.getAbilities().stream().anyMatch(ability -> ability instanceof ReboundAbility);
            if (!found) {
                Ability ability = new ReboundAbility();
                game.getState().addOtherAbility(card, ability);
            }
        }
    }
}
