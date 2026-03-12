import java.util.*;

/**
 * SkillSwapDSA.java
 * Demonstrates all DSA concepts used in SkillSwap — Campus Skill Exchange Network
 * Student: T. Sai Sri Parinita | Roll No: 2520030562 | Cluster: 1 | KL University Bachupally
 */
public class SkillSwapDSA {

    // ═══════════════════════════════════════════════════════════
    //  DATA MODELS
    // ═══════════════════════════════════════════════════════════

    static class Student {
        int id;
        String name;
        String dept;
        List<String> teaches;
        List<String> wants;
        int rep; // reputation points: completed*30 + accepted*10

        Student(int id, String name, String dept, List<String> teaches, List<String> wants, int rep) {
            this.id = id; this.name = name; this.dept = dept;
            this.teaches = teaches; this.wants = wants; this.rep = rep;
        }

        @Override
        public String toString() {
            return String.format("%-18s %-6s teaches:%-25s wants:%-20s rep:%d",
                name, dept, teaches.toString(), wants.toString(), rep);
        }
    }

    static class Request {
        String from, to, skill, status;
        Request(String from, String to, String skill, String status) {
            this.from = from; this.to = to; this.skill = skill; this.status = status;
        }
        @Override public String toString() {
            return from + " → " + to + " [" + skill + "] (" + status + ")";
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  LINKED LIST — Skill Exchange History
    // ═══════════════════════════════════════════════════════════

    static class SkillNode {
        String skill;
        String exchangedWith;
        SkillNode next;

        SkillNode(String skill, String exchangedWith) {
            this.skill = skill;
            this.exchangedWith = exchangedWith;
            this.next = null;
        }
    }

    static class SkillExchangeHistory {
        SkillNode head;
        int size;

        // INSERT at front — O(1)
        void insert(String skill, String partner) {
            SkillNode newNode = new SkillNode(skill, partner);
            newNode.next = head;
            head = newNode;
            size++;
            System.out.println("  [INSERT] Added exchange: " + skill + " with " + partner);
        }

        // DELETE by skill name — O(n)
        void delete(String skill) {
            if (head == null) return;
            if (head.skill.equalsIgnoreCase(skill)) {
                System.out.println("  [DELETE] Removed: " + head.skill + " with " + head.exchangedWith);
                head = head.next; size--; return;
            }
            SkillNode curr = head;
            while (curr.next != null) {
                if (curr.next.skill.equalsIgnoreCase(skill)) {
                    System.out.println("  [DELETE] Removed: " + curr.next.skill + " with " + curr.next.exchangedWith);
                    curr.next = curr.next.next; size--; return;
                }
                curr = curr.next;
            }
            System.out.println("  [DELETE] Skill '" + skill + "' not found.");
        }

        // TRAVERSAL — O(n)
        void traverse() {
            System.out.println("  Exchange History (newest first):");
            SkillNode curr = head;
            int i = 1;
            while (curr != null) {
                System.out.println("    " + i++ + ". " + curr.skill + " ↔ " + curr.exchangedWith);
                curr = curr.next;
            }
        }

        // SEARCH — O(n)
        boolean search(String skill) {
            SkillNode curr = head;
            while (curr != null) {
                if (curr.skill.equalsIgnoreCase(skill)) {
                    System.out.println("  [SEARCH] Found '" + skill + "' exchanged with: " + curr.exchangedWith);
                    return true;
                }
                curr = curr.next;
            }
            System.out.println("  [SEARCH] '" + skill + "' not found in history.");
            return false;
        }

        // REVERSE — O(n)
        void reverse() {
            SkillNode prev = null, curr = head, next = null;
            while (curr != null) {
                next = curr.next;
                curr.next = prev;
                prev = curr;
                curr = next;
            }
            head = prev;
            System.out.println("  [REVERSE] History reversed.");
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  STACK — Request Undo History
    // ═══════════════════════════════════════════════════════════

    static Stack<Request> requestStack = new Stack<>();

    static void pushRequest(String from, String to, String skill) {
        Request r = new Request(from, to, skill, "pending");
        requestStack.push(r);
        System.out.println("  [PUSH]  " + r);
    }

    static void popRequest() {
        if (!requestStack.isEmpty()) {
            Request r = requestStack.pop();
            System.out.println("  [POP]   Undone: " + r);
        }
    }

    // Parentheses/bracket check — Stack application
    static boolean checkBalanced(String expr) {
        Stack<Character> s = new Stack<>();
        for (char c : expr.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') s.push(c);
            else if (c == ')' || c == '}' || c == ']') {
                if (s.isEmpty()) return false;
                char top = s.pop();
                if ((c==')' && top!='(') || (c=='}' && top!='{') || (c==']' && top!='[')) return false;
            }
        }
        return s.isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    //  QUEUE — Request Processing (FIFO)
    // ═══════════════════════════════════════════════════════════

    static Queue<Request> requestQueue = new LinkedList<>();

    static void enqueueRequest(String from, String to, String skill) {
        Request r = new Request(from, to, skill, "pending");
        requestQueue.add(r);
        System.out.println("  [ENQUEUE] " + r);
    }

    static void dequeueRequest() {
        if (!requestQueue.isEmpty()) {
            Request r = requestQueue.remove();
            r.status = "processing";
            System.out.println("  [DEQUEUE] Processing: " + r);
        }
    }

    // Circular Queue — for round-robin session scheduling
    static int[] circularQueue = new int[5];
    static int front = -1, rear = -1, cqSize = 0;
    static final int CQ_MAX = 5;

    static void cqEnqueue(int sessionId) {
        if (cqSize == CQ_MAX) { System.out.println("  [CQ] Queue full"); return; }
        if (front == -1) front = 0;
        rear = (rear + 1) % CQ_MAX;
        circularQueue[rear] = sessionId;
        cqSize++;
        System.out.println("  [CQ ENQUEUE] Session " + sessionId + " scheduled");
    }

    static void cqDequeue() {
        if (cqSize == 0) { System.out.println("  [CQ] Queue empty"); return; }
        System.out.println("  [CQ DEQUEUE] Processing Session " + circularQueue[front]);
        front = (front + 1) % CQ_MAX;
        cqSize--;
        if (cqSize == 0) { front = -1; rear = -1; }
    }

    // ═══════════════════════════════════════════════════════════
    //  SEARCHING
    // ═══════════════════════════════════════════════════════════

    // Linear Search — O(n) — find all teachers of a skill
    static void linearSearch(List<Student> students, String skill) {
        System.out.println("  Linear Search for skill: '" + skill + "' — O(n)");
        boolean found = false;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).teaches.stream().anyMatch(s -> s.equalsIgnoreCase(skill))) {
                System.out.println("    ✔ Found at index [" + i + "]: " + students.get(i).name);
                found = true;
            }
        }
        if (!found) System.out.println("    ✘ No teacher found for '" + skill + "'");
    }

    // Binary Search — O(log n) — search by rep score in SORTED array
    static int binarySearch(List<Student> sortedStudents, int targetRep) {
        int low = 0, high = sortedStudents.size() - 1;
        System.out.println("  Binary Search for rep=" + targetRep + " — O(log n)");
        while (low <= high) {
            int mid = (low + high) / 2;
            int midRep = sortedStudents.get(mid).rep;
            System.out.println("    Checking mid=" + mid + " rep=" + midRep);
            if (midRep == targetRep) {
                System.out.println("    ✔ Found: " + sortedStudents.get(mid).name + " at index " + mid);
                return mid;
            } else if (midRep < targetRep) low = mid + 1;
            else high = mid - 1;
        }
        System.out.println("    ✘ Rep score " + targetRep + " not found.");
        return -1;
    }

    // ═══════════════════════════════════════════════════════════
    //  SORTING
    // ═══════════════════════════════════════════════════════════

    // Bubble Sort — O(n²) — leaderboard by rep
    static void bubbleSort(List<Student> students) {
        int n = students.size(), swaps = 0;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (students.get(j).rep < students.get(j + 1).rep) {
                    Student tmp = students.get(j);
                    students.set(j, students.get(j + 1));
                    students.set(j + 1, tmp);
                    swaps++;
                }
            }
        }
        System.out.println("  Bubble Sort complete — " + swaps + " swaps — O(n²)");
    }

    // Selection Sort — sort by name alphabetically
    static void selectionSort(List<Student> students) {
        int n = students.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (students.get(j).name.compareTo(students.get(minIdx).name) < 0)
                    minIdx = j;
            }
            Student tmp = students.get(minIdx);
            students.set(minIdx, students.get(i));
            students.set(i, tmp);
        }
        System.out.println("  Selection Sort complete — sorted alphabetically — O(n²)");
    }

    // Merge Sort — O(n log n) — efficient leaderboard sorting
    static void mergeSort(List<Student> students, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(students, left, mid);
            mergeSort(students, mid + 1, right);
            merge(students, left, mid, right);
        }
    }

    static void merge(List<Student> students, int left, int mid, int right) {
        List<Student> L = new ArrayList<>(students.subList(left, mid + 1));
        List<Student> R = new ArrayList<>(students.subList(mid + 1, right + 1));
        int i = 0, j = 0, k = left;
        while (i < L.size() && j < R.size()) {
            if (L.get(i).rep >= R.get(j).rep) students.set(k++, L.get(i++));
            else students.set(k++, R.get(j++));
        }
        while (i < L.size()) students.set(k++, L.get(i++));
        while (j < R.size()) students.set(k++, R.get(j++));
    }

    // Quick Sort — O(n log n) avg — sort matches by compatibility score
    static void quickSort(List<Student> students, int low, int high) {
        if (low < high) {
            int pi = partition(students, low, high);
            quickSort(students, low, pi - 1);
            quickSort(students, pi + 1, high);
        }
    }

    static int partition(List<Student> students, int low, int high) {
        int pivot = students.get(high).rep;
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (students.get(j).rep >= pivot) {
                i++;
                Student tmp = students.get(i);
                students.set(i, students.get(j));
                students.set(j, tmp);
            }
        }
        Student tmp = students.get(i + 1);
        students.set(i + 1, students.get(high));
        students.set(high, tmp);
        return i + 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  HASHING — Skill-to-Student Map
    // ═══════════════════════════════════════════════════════════

    static final int TABLE_SIZE = 10;
    static List<String[]>[] hashTable = new ArrayList[TABLE_SIZE]; // chaining

    static int hashFunction(String key) {
        int hash = 0;
        for (char c : key.toLowerCase().toCharArray()) hash = (hash * 31 + c) % TABLE_SIZE;
        return Math.abs(hash);
    }

    static void hashInsert(String skill, String teacherName) {
        int idx = hashFunction(skill);
        if (hashTable[idx] == null) hashTable[idx] = new ArrayList<>();
        hashTable[idx].add(new String[]{skill, teacherName});
        System.out.println("  [HASH INSERT] '" + skill + "' → bucket[" + idx + "] — teacher: " + teacherName);
    }

    static void hashSearch(String skill) {
        int idx = hashFunction(skill);
        System.out.println("  [HASH SEARCH] '" + skill + "' → bucket[" + idx + "]");
        if (hashTable[idx] != null) {
            for (String[] entry : hashTable[idx]) {
                if (entry[0].equalsIgnoreCase(skill))
                    System.out.println("    ✔ Found: " + entry[0] + " taught by " + entry[1]);
            }
        } else System.out.println("    ✘ Not found.");
    }

    static void hashDelete(String skill) {
        int idx = hashFunction(skill);
        if (hashTable[idx] != null) {
            hashTable[idx].removeIf(e -> e[0].equalsIgnoreCase(skill));
            System.out.println("  [HASH DELETE] '" + skill + "' removed from bucket[" + idx + "]");
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║         SKILLSWAP — DSA DEMONSTRATION                ║");
        System.out.println("║  Campus Skill Exchange Network — KL University       ║");
        System.out.println("║  Student: T. Sai Sri Parinita | Roll: 2520030562     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        // ── Sample Data ──────────────────────────────────────────
        List<Student> students = new ArrayList<>(Arrays.asList(
            new Student(1,"Arjun Sharma",  "CSE", Arrays.asList("Python","DSA"),          Arrays.asList("UI Design"),       120),
            new Student(2,"Priya Nair",    "ECE", Arrays.asList("UI Design","Figma"),      Arrays.asList("Python"),          95),
            new Student(3,"Rohan Mehta",   "CSE", Arrays.asList("React","JavaScript"),     Arrays.asList("Machine Learning"),150),
            new Student(4,"Sneha Patel",   "IT",  Arrays.asList("Machine Learning"),       Arrays.asList("React"),           80),
            new Student(5,"Karan Iyer",    "EEE", Arrays.asList("Arduino","IoT"),          Arrays.asList("DSA"),             60),
            new Student(6,"Divya Krishnan","CSE", Arrays.asList("DSA","C++"),              Arrays.asList("Figma"),           110),
            new Student(7,"Meera Reddy",   "IT",  Arrays.asList("Data Analysis","Python"), Arrays.asList("React"),           75)
        ));

        // ──────────────────────────────────────────────────────────
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("1. ARRAY / LIST — Student Skill Profiles");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Total registered students: " + students.size());
        students.forEach(s -> System.out.println("  " + s));

        // ──────────────────────────────────────────────────────────
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("2. LINKED LIST — Skill Exchange History");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        SkillExchangeHistory history = new SkillExchangeHistory();
        history.insert("Python",    "Priya Nair");
        history.insert("UI Design", "Arjun Sharma");
        history.insert("React",     "Sneha Patel");
        history.insert("DSA",       "Karan Iyer");
        history.traverse();
        history.search("React");
        history.search("Figma");
        history.delete("UI Design");
        history.reverse();
        history.traverse();

        // ──────────────────────────────────────────────────────────
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("3. STACK — Request Undo History");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        pushRequest("Arjun",  "Priya",  "UI Design");
        pushRequest("Sneha",  "Rohan",  "React");
        pushRequest("Karan",  "Arjun",  "DSA");
        System.out.println("  Top of stack: " + requestStack.peek());
        popRequest();
        System.out.println("  Stack size after pop: " + requestStack.size());

        System.out.println("  Parentheses Check (Stack Application):");
        String[] exprs = {"(Python + {DSA})", "(React + [UI)", "{{Figma}}"};
        for (String e : exprs)
            System.out.println("    " + e + " → " + (checkBalanced(e) ? "✔ Balanced" : "✘ Not balanced"));

        // ──────────────────────────────────────────────────────────
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("4. QUEUE — Request Processing (FIFO)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        enqueueRequest("Divya", "Priya",  "Figma");
        enqueueRequest("Priya", "Arjun",  "Python");
        enqueueRequest("Karan", "Divya",  "C++");
        dequeueRequest();
        dequeueRequest();
        System.out.println("  Remaining in queue: " + requestQueue.size());

        System.out.println("  Circular Queue — Session Scheduling:");
        cqEnqueue(101); cqEnqueue(102); cqEnqueue(103);
        cqDequeue(); cqDequeue();
        cqEnqueue(104); cqEnqueue(105);
        cqDequeue();

        // ──────────────────────────────────────────────────────────
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("5. SEARCHING");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        linearSearch(students, "DSA");
        linearSearch(students, "Figma");

        // Sort by rep ascending for binary search
        List<Student> sortedAsc = new ArrayList<>(students);
        sortedAsc.sort(Comparator.comparingInt(s -> s.rep));
        System.out.println("  Sorted by rep (asc): ");
        sortedAsc.forEach(s -> System.out.println("    " + s.name + " rep=" + s.rep));
        binarySearch(sortedAsc, 110);
        binarySearch(sortedAsc, 999);

        // ──────────────────────────────────────────────────────────
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("6. SORTING");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        List<Student> s1 = new ArrayList<>(students);
        bubbleSort(s1);
        System.out.println("  Bubble Sort — Leaderboard:");
        s1.forEach(s -> System.out.println("    " + s.name + " — " + s.rep + " pts"));

        List<Student> s2 = new ArrayList<>(students);
        selectionSort(s2);
        System.out.println("  Selection Sort — Alphabetical:");
        s2.forEach(s -> System.out.println("    " + s.name));

        List<Student> s3 = new ArrayList<>(students);
        mergeSort(s3, 0, s3.size() - 1);
        System.out.println("  Merge Sort — Leaderboard O(n log n):");
        s3.forEach(s -> System.out.println("    " + s.name + " — " + s.rep + " pts"));

        List<Student> s4 = new ArrayList<>(students);
        quickSort(s4, 0, s4.size() - 1);
        System.out.println("  Quick Sort — Leaderboard O(n log n) avg:");
        s4.forEach(s -> System.out.println("    " + s.name + " — " + s.rep + " pts"));

        // ──────────────────────────────────────────────────────────
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("7. HASHING — Skill Directory (O(1) lookup)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        for (int i = 0; i < TABLE_SIZE; i++) hashTable[i] = null;
        students.forEach(s -> s.teaches.forEach(skill -> hashInsert(skill, s.name)));
        System.out.println();
        hashSearch("Python");
        hashSearch("DSA");
        hashSearch("Blockchain");
        hashDelete("Figma");
        hashSearch("Figma");

        // ──────────────────────────────────────────────────────────
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║  All DSA concepts demonstrated successfully!         ║");
        System.out.println("║  SkillSwap — Campus Skill Exchange Network           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
}
