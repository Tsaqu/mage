/*
* Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification, are
* permitted provided that the following conditions are met:
*
*    1. Redistributions of source code must retain the above copyright notice, this list of
*       conditions and the following disclaimer.
*
*    2. Redistributions in binary form must reproduce the above copyright notice, this list
*       of conditions and the following disclaimer in the documentation and/or other materials
*       provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
* FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation are those of the
* authors and should not be interpreted as representing official policies, either expressed
* or implied, of BetaSteward_at_googlemail.com.
*/

package mage.abilities.effects.common.continious;

import mage.Constants.Duration;
import mage.Constants.Layer;
import mage.Constants.Outcome;
import mage.Constants.SubLayer;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.game.Game;
import mage.players.Player;

/**
 * @author nantuko
 */
public class MaximumHandSizeControllerEffect extends ContinuousEffectImpl<MaximumHandSizeControllerEffect> {

	protected int handSize;
	protected boolean reduce;

	/**
	 * @param handSize Maximum hand size to set or to reduce by
	 * @param duration Effect duration
	 * @param reduce   If true, the hand size will be reduced related to current value, otherwise it will be set.
	 */
	public MaximumHandSizeControllerEffect(int handSize, Duration duration, boolean reduce) {
		super(duration, Layer.PlayerEffects, SubLayer.NA, Outcome.Benefit);
		this.handSize = handSize;
		this.reduce = reduce;
		setText();
	}

	public MaximumHandSizeControllerEffect(final MaximumHandSizeControllerEffect effect) {
		super(effect);
		this.handSize = effect.handSize;
		this.reduce = effect.reduce;
	}

	@Override
	public MaximumHandSizeControllerEffect copy() {
		return new MaximumHandSizeControllerEffect(this);
	}

	@Override
	public boolean apply(Game game, Ability source) {
		Player player = game.getPlayer(source.getControllerId());
		if (player != null) {
			if (reduce) {
				player.setMaxHandSize(player.getMaxHandSize() - handSize);
			} else {
				player.setMaxHandSize(handSize);
			}
			return true;
		}
		return true;
	}

	private void setText() {

		if (handSize == Integer.MAX_VALUE) {
			staticText = "You have no maximum hand size";
		} else {
			StringBuilder sb = new StringBuilder("Your maximum hand size is ");
			if (reduce) {
				sb.append("reduced by ");
				sb.append(Integer.toString(handSize));
			} else {
				sb.append(Integer.toString(handSize));
			}
			staticText = sb.toString();
		}
	}

}