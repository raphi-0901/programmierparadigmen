import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        System.out.println(getAnnotationEvaluation());
    }

    private static String getAnnotationEvaluation() {
        StringBuilder result = new StringBuilder();
//        Class[] classes = new Class[]{Formicarium.class};
        Class[] classes = new Class[]{CodedBy.class, Entity.class, Formicarium.class, Institute.class, Nest.class, NestArrayList.class, SignatureAndAssertions.class, Test.class};
        HashMap<String, AnnotationObject> infos = new HashMap<>();
        HashMap<String, ClassAnnotationObject> assertionsPerClass = new HashMap<>();

        for (Class aClass : classes) {
            List<AccessibleObject> members = new ArrayList<>();
            members.addAll(Arrays.asList(aClass.getMethods()));
            members.addAll(Arrays.asList(aClass.getConstructors()));

            // read all methods from class
            for (AccessibleObject member : members) {
                // read all annotations from method
                String responsiblePerson = "";
                boolean wroteAssertion = false;
                for (Annotation annotation : member.getAnnotations()) {
                    // check for different cases
                    if (annotation instanceof CodedBy codedBy) {
                        responsiblePerson = codedBy.value();
                        if (aClass.isInterface() || aClass.isAnnotation()) {
                            continue;
                        }

                        AnnotationObject info = infos.computeIfAbsent(responsiblePerson, k -> new AnnotationObject());

                        // if member is constructor, add to constructor stats
                        if(member instanceof Constructor<?>) {
                            HashMap<String, Integer> constructorsInClass = info.getConstructorsInClass();
                            int newValue = constructorsInClass.getOrDefault(aClass.getName(), 0) + 1;
                            constructorsInClass.put(aClass.getName(), newValue);
                        }

                        if(member instanceof Method) {
                            // get the amount of methods per class
                            HashMap<String, Integer> methodsInClass = info.getMethodsInClass();
                            int newValue = methodsInClass.getOrDefault(aClass.getName(), 0) + 1;
                            methodsInClass.put(aClass.getName(), newValue);
                        }
                    }

                    if (annotation instanceof SignatureAndAssertions signatureAndAssertions) {
                        wroteAssertion = true;

                        ClassAnnotationObject classAnnotationObject = assertionsPerClass.computeIfAbsent(aClass.getName(), k -> new ClassAnnotationObject());
                        classAnnotationObject.getAssertionsPerMethod().put(member.toString(), signatureAndAssertions);
                    }
                }

                if(!responsiblePerson.isEmpty() && wroteAssertion) {
                    infos.get(responsiblePerson).increaseAssertions();
                }
            }

            Annotation[] declaredAnnotation = aClass.getDeclaredAnnotations();
            for (Annotation annotation : declaredAnnotation) {
                if (annotation instanceof CodedBy codedBy) {
                    String name = codedBy.value();
                    AnnotationObject info = infos.get(name);
                    if (info == null) {
                        info = new AnnotationObject();
                        infos.put(name, info);
                    }

                    List<String> list = infos.getOrDefault(name, new AnnotationObject()).getResponsibleEntities();
                    list.add(aClass.getName());
                }
            }
        }

        infos.forEach((key, value) -> {
            result.append("Stats for: ").append(key).append("\n");
            value.getMethodsInClass().forEach((key1, value1) -> {
                result.append(key1).append(":\t").append(value1).append(" method(s)");
            });

            if(!value.getMethodsInClass().isEmpty()) {
                result.append("\n");
            }

            value.getConstructorsInClass().forEach((key1, value1) -> {
                result.append(key1).append(":\t").append(value1).append(" constructor(s)");
            });

            if(!value.getConstructorsInClass().isEmpty()) {
                result.append("\n");
            }

            result.append("Responsible for ").append(value.getResponsibleEntities().size()).append(" units: ");
            value.getResponsibleEntities().forEach((value1) -> result.append(value1).append(", "));

            result.delete(result.length() - 2, result.length()).append("\n");

            result.append("wrote ").append(value.getAssertions()).append(" assertions");
            result.append("\n\n");
        });

        result.append("Zusicherungen:\n");

        assertionsPerClass.forEach((key, value) -> {
            result.append("---------------------------------------------------------\n");
            result.append(key).append("\n\n");

            value.getAssertionsPerMethod().forEach((key1, value1) -> {
                result.append(key1).append("\n");
                result.append("Preconiditions: ").append(value1.preconditions()).append("\n");
                result.append("Postconditions: ").append(value1.postconditions()).append("\n");
                result.append("Invariants: ").append(value1.invariants()).append("\n");
                result.append("History-Constraints: ").append(value1.historyConstrains()).append("\n\n");
            });
        });


        return result.toString();
    }
}
