/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.sponge;

import com.google.common.base.Splitter;

import me.lucko.luckperms.common.commands.CommandManager;
import me.lucko.luckperms.common.commands.sender.Sender;
import me.lucko.luckperms.common.commands.utils.Util;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandExecutor extends CommandManager implements CommandCallable {
    private static final Splitter ARGUMENT_SPLITTER = Splitter.on(COMMAND_SEPARATOR_PATTERN).omitEmptyStrings();

    private final LPSpongePlugin plugin;

    SpongeCommandExecutor(LPSpongePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String s) {
        Sender lpSender = plugin.getSenderFactory().wrap(source);
        List<String> arguments = processSelectors(source, Util.stripQuotes(ARGUMENT_SPLITTER.splitToList(s)));

        onCommand(lpSender, "lp", arguments);
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String s, @Nullable Location<World> location) {
        Sender lpSender = plugin.getSenderFactory().wrap(source);
        List<String> arguments = processSelectors(source, Util.stripQuotes(ARGUMENT_SPLITTER.splitToList(s)));

        return onTabComplete(lpSender, arguments);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true; // we run permission checks internally
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Manage permissions"));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("Run /luckperms to view usage."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("/luckperms");
    }

    private List<String> processSelectors(CommandSource source, List<String> args) {
        ListIterator<String> it = args.listIterator();
        while (it.hasNext()) {
            String element = it.next();
            if (element.startsWith("@")) {
                try {
                    Player ret = Selector.parse(element).resolve(source).stream()
                            .filter(e -> e instanceof Player)
                            .map(e -> ((Player) e))
                            .findFirst().orElse(null);

                    if (ret != null) {
                        it.set(ret.getUniqueId().toString());
                    }
                } catch (IllegalArgumentException e) {
                    // ignored
                }
            }
        }
        return args;
    }

}
