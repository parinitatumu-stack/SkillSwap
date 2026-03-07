
import java.util.*;

public class SkillSwapDSA {

    static class Student {
        int id;
        String name;
        String dept;
        List<String> teaches;
        List<String> wants;
        int rep;

        Student(int id, String name, String dept, List<String> teaches, List<String> wants, int rep) {
            this.id = id;
            this.name = name;
            this.dept = dept;
            this.teaches = teaches;
            this.wants = wants;
            this.rep = rep;
        }
    }

    static class Request {
        String from;
        String to;
        String skill;

        Request(String from, String to, String skill) {
            this.from = from;
            this.to = to;
            this.skill = skill;
        }
    }

    static Stack<Request> requestStack = new Stack<>();
    static Queue<Request> requestQueue = new LinkedList<>();

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("     SKILLSWAP — DSA DEMONSTRATION");
        System.out.println("======================================\n");

        System.out.println("1. ARRAY / LIST — Student Profiles\n");

        List<Student> students = new ArrayList<>();

        students.add(new Student(1,"Arjun Sharma","CSE",
                Arrays.asList("Python","DSA"),
                Arrays.asList("UI Design"),120));

        students.add(new Student(2,"Priya Nair","ECE",
                Arrays.asList("UI Design","Figma"),
                Arrays.asList("Python"),95));

        students.add(new Student(3,"Rohan Mehta","CSE",
                Arrays.asList("React","JavaScript"),
                Arrays.asList("Machine Learning"),150));

        students.add(new Student(4,"Sneha Patel","IT",
                Arrays.asList("Machine Learning"),
                Arrays.asList("React"),80));

        students.add(new Student(5,"Karan Iyer","EEE",
                Arrays.asList("Arduino","IoT"),
                Arrays.asList("DSA"),60));

        students.add(new Student(6,"Divya Krishnan","CSE",
                Arrays.asList("DSA","C++"),
                Arrays.asList("Figma"),110));

        System.out.println("Total students: " + students.size());

        for(Student s : students){
            System.out.println(s.name + " (" + s.dept + ") teaches " + s.teaches);
        }

        System.out.println("\n2. STACK — Request History");

        pushRequest("Arjun","Priya","UI Design");
        pushRequest("Sneha","Rohan","React");
        pushRequest("Karan","Arjun","DSA");

        System.out.println("Top Request: " + requestStack.peek().skill);

        popRequest();

        System.out.println("\n3. QUEUE — Pending Requests");

        enqueueRequest("Divya","Priya","Figma");
        enqueueRequest("Priya","Arjun","Python");
        enqueueRequest("Karan","Divya","C++");

        dequeueRequest();

        System.out.println("\n4. LINEAR SEARCH — Find Skill");

        linearSearch(students,"DSA");

        System.out.println("\n5. BUBBLE SORT — Leaderboard");

        bubbleSort(students);

        System.out.println("\nLeaderboard:");
        for(Student s : students){
            System.out.println(s.name + " - " + s.rep + " points");
        }
    }

    static void pushRequest(String from,String to,String skill){
        Request r = new Request(from,to,skill);
        requestStack.push(r);
        System.out.println("Push: "+from+" requested "+skill);
    }

    static void popRequest(){
        Request r = requestStack.pop();
        System.out.println("Pop: Removed request "+r.skill);
    }

    static void enqueueRequest(String from,String to,String skill){
        Request r = new Request(from,to,skill);
        requestQueue.add(r);
        System.out.println("Enqueue: "+from+" requested "+skill);
    }

    static void dequeueRequest(){
        Request r = requestQueue.remove();
        System.out.println("Dequeue: Processing "+r.skill+" request");
    }

    static void linearSearch(List<Student> students,String skill){
        for(Student s : students){
            if(s.teaches.contains(skill)){
                System.out.println("Found "+skill+" teacher: "+s.name);
            }
        }
    }

    static void bubbleSort(List<Student> students){
        int n = students.size();

        for(int i=0;i<n-1;i++){
            for(int j=0;j<n-i-1;j++){
                if(students.get(j).rep < students.get(j+1).rep){
                    Student temp = students.get(j);
                    students.set(j,students.get(j+1));
                    students.set(j+1,temp);
                }
            }
        }
    }
}
