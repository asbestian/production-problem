package de.asbestian.lotsizing.algorithm;

import de.asbestian.lotsizing.graph.Schedule;
import de.asbestian.lotsizing.input.Input;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import org.paukov.combinatorics3.Generator;

/**
 * Class representing potential change over permutations which corresponds to cost improvements.
 *
 * @author Sebastian Schenker
 */
public class ChangeOver {

  private final Input input;
  private final IntList scheduleWithoutIdleSlots;

  public ChangeOver(final Input input, final Schedule schedule) {
    this.input = input;
    this.scheduleWithoutIdleSlots = schedule.getScheduleWithoutIdleSlots();
  }

  public List<IntList> computeCandidates(final int maxCardinality) {
    final List<IntList> candidates = new ArrayList<>();
    for (int start = 0; start <= scheduleWithoutIdleSlots.size() - maxCardinality; ++start) {
      final int endExlusive = start + maxCardinality;
      final var subList = scheduleWithoutIdleSlots.subList(start, endExlusive);
      final double currentChangeOverCost =
          getPartialChangeOverCost(start, endExlusive - 1, subList);

      final int finalStart = start;
      Generator.permutation(subList).simple().stream()
          .filter(
              perm ->
                  getPartialChangeOverCost(finalStart, endExlusive - 1, perm)
                          - currentChangeOverCost
                      < 0.)
          .map(IntArrayList::new)
          .forEach(candidates::add);
    }
    return candidates;
  }

  public double getPartialChangeOverCost(
      final int startIndex, final int endInclusive, final List<Integer> subSchedule) {
    double changeOverCost = 0;
    for (int i = 0; i < subSchedule.size() - 1; ++i) {
      final int predType = subSchedule.get(i);
      final int succType = subSchedule.get(i + 1);
      changeOverCost += input.getChangeOverCost(predType, succType);
    }
    return changeOverCost
        + getLeftBorderChangeOverCosts(startIndex, subSchedule.get(0))
        + getRightBorderChangeOverCosts(endInclusive, subSchedule.get(subSchedule.size() - 1));
  }

  private double getLeftBorderChangeOverCosts(final int index, final int machineType) {
    return index == 0
        ? 0.
        : input.getChangeOverCost(scheduleWithoutIdleSlots.getInt(index - 1), machineType);
  }

  private double getRightBorderChangeOverCosts(final int index, final int machineType) {
    return index == scheduleWithoutIdleSlots.size() - 1
        ? 0.
        : input.getChangeOverCost(machineType, scheduleWithoutIdleSlots.getInt(index + 1));
  }
}
