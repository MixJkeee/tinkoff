package ru.mxjkeee;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static ru.mxjkeee.Constants.CONSOLE_DELIMITER;

public class ListsVerifier<T> {

    public void checkTwoListsOnCommonElements(List<T> referenceList, List<T> listToVerify) {
        System.out.println(getElementsNotContainedInReferenceList(referenceList, listToVerify));
        System.out.println(getElementsNotContainedInReferenceList(listToVerify, referenceList));
    }

    private List<T> getElementsNotContainedInReferenceList(List<T> referenceList, List<T> listToVerify) {
        return listToVerify.stream()
                .filter(item -> !referenceList.contains(item))
                .collect(toList());
    }

    public static void main(String[] args) {
        System.out.println("Running task for checking lists on excess elements");
        System.out.println(CONSOLE_DELIMITER);
        ListsVerifier<String> listsVerifier = new ListsVerifier<>();
        List<String> referenceList = asList("A", "B", "C", "D");
        List<String> listToVerify = asList("A", "B", "C", "D", "E");
        listsVerifier.checkTwoListsOnCommonElements(referenceList, listToVerify);
        listToVerify = asList("A", "B", "C", "E");
        listsVerifier.checkTwoListsOnCommonElements(referenceList, listToVerify);
    }
}
