import java.util.*;
import java.io.*;

/**
 * =======================================================================
 *  SkillSwapDSA_Final.java
 *  Campus Skill Exchange Network -- Data Structures & Algorithms Demo
 * -----------------------------------------------------------------------
 *  Student  : T. Sai Sri Parinita
 *  Roll No  : 2520030562
 *  Cluster  : 1
 *  College  : KL University Bachupally
 * -----------------------------------------------------------------------
 *  DSA concepts demonstrated (mapped to SkillSwap_Final.html logic):
 *
 *   1. Arrays / ArrayLists    -- Student profile store
 *   2. Singly Linked List     -- Exchange history (insert/delete/
 *                                traverse/search/reverse)
 *   3. Doubly Linked List     -- Bidirectional skill catalogue
 *   4. Cycle Detection        -- Floyd's Tortoise & Hare
 *   5. Stack                  -- Undo request history +
 *                                Parentheses / expression checker
 *   6. Queue (FIFO)           -- Request processing pipeline
 *   7. Circular Queue         -- Round-robin session scheduler
 *   8. Linear Search          -- Find skill teachers         O(n)
 *   9. Binary Search          -- Find student by rep score   O(log n)
 *  10. Bubble Sort            -- Leaderboard (rep desc)      O(n^2)
 *  11. Selection Sort         -- Alphabetical name sort      O(n^2)
 *  12. Insertion Sort         -- Incremental leaderboard     O(n^2)
 *  13. Merge Sort             -- Full campus leaderboard     O(n log n)
 *  14. Quick Sort             -- Match ranking by score      O(n log n)
 *  15. Hashing (chaining)     -- Skill directory             O(1) avg
 *  16. Matching Algorithm     -- Mirrors JS renderMatches()
 * =======================================================================
 */
public class SkillSwapDSA_Final {

    // ===================================================================
    //  CONFIGURE OUTPUT -- set to true to also save output to a .txt file
    // ===================================================================
    static final boolean SAVE_TO_FILE = false;
    static PrintStream out = System.out;


    // ===================================================================
    //  1. DATA MODELS
    //     Student mirrors a Firestore document in SkillSwap_Final.html:
    //     { uid, name, dept, skillsOffered(teaches), skillsWanted(wants),
    //       rep: completed*30 + accepted*10 }
    // ===================================================================

    static class Student {
        int    id;
        String name, dept;
        List<String> teaches;   // skillsOffered in Firestore
        List<String> wants;     // skillsWanted  in Firestore
        int    rep;             // reputation points
        int    matchScore;      // computed by matching algorithm

        Student(int id, String name, String dept,
                List<String> teaches, List<String> wants, int rep) {
            this.id = id; this.name = name; this.dept = dept;
            this.teaches = teaches; this.wants = wants;
            this.rep = rep; this.matchScore = 0;
        }

        @Override public String toString() {
            return String.format("  [%d] %-18s | %-4s | Teaches: %-32s | Wants: %-25s | Rep: %d",
                id, name, dept, teaches.toString(), wants.toString(), rep);
        }
    }

    /**
     * Request mirrors the JS REQUESTS object.
     * Status flow:  pending --> accepted --> completed
     */
    static class Request {
        String from, to, skill, status;
        Request(String from, String to, String skill, String status) {
            this.from = from; this.to = to;
            this.skill = skill; this.status = status;
        }
        @Override public String toString() {
            return String.format("  %-12s -> %-15s | Skill: %-20s | Status: %s",
                from, to, skill, status);
        }
    }


    // ===================================================================
    //  2. SINGLY LINKED LIST -- Skill Exchange History
    //     Powers the Tracker screen "Exchange History" feed.
    //     insert O(1) | delete O(n) | traverse O(n) |
    //     search O(n) | reverse O(n)
    // ===================================================================

    static class SkillNode {
        String skill, exchangedWith;
        SkillNode next;
        SkillNode(String skill, String exchangedWith) {
            this.skill = skill;
            this.exchangedWith = exchangedWith;
        }
    }

    static class SkillExchangeHistory {
        SkillNode head;
        int size = 0;

        /** INSERT at head -- O(1). New exchange prepended so latest shows first. */
        void insert(String skill, String partner) {
            SkillNode node = new SkillNode(skill, partner);
            node.next = head;
            head = node;
            size++;
            out.println("    [INSERT] Exchange added : " + skill + " <-> " + partner);
        }

        /** DELETE by skill name -- O(n). Called when user removes an entry. */
        void delete(String skill) {
            if (head == null) { out.println("    [DELETE] List is empty."); return; }
            if (head.skill.equalsIgnoreCase(skill)) {
                out.println("    [DELETE] Removed head  : " + head.skill + " <-> " + head.exchangedWith);
                head = head.next; size--; return;
            }
            SkillNode curr = head;
            while (curr.next != null) {
                if (curr.next.skill.equalsIgnoreCase(skill)) {
                    out.println("    [DELETE] Removed       : " + curr.next.skill + " <-> " + curr.next.exchangedWith);
                    curr.next = curr.next.next; size--; return;
                }
                curr = curr.next;
            }
            out.println("    [DELETE] '" + skill + "' not found.");
        }

        /** TRAVERSAL -- O(n). Renders the full exchange history list. */
        void traverse() {
            out.println("    Exchange History (newest first):");
            if (head == null) { out.println("      (empty)"); return; }
            SkillNode curr = head;
            int i = 1;
            while (curr != null) {
                out.printf("      %d. %-20s <-> %s%n", i++, curr.skill, curr.exchangedWith);
                curr = curr.next;
            }
            out.println("    Total entries: " + size);
        }

        /** SEARCH -- O(n). Filter history by skill name. */
        boolean search(String skill) {
            SkillNode curr = head;
            while (curr != null) {
                if (curr.skill.equalsIgnoreCase(skill)) {
                    out.println("    [SEARCH OK] '" + skill + "' found -- exchanged with: " + curr.exchangedWith);
                    return true;
                }
                curr = curr.next;
            }
            out.println("    [SEARCH --] '" + skill + "' not found in history.");
            return false;
        }

        /** REVERSE -- O(n). View history oldest-first. */
        void reverse() {
            SkillNode prev = null, curr = head, next;
            while (curr != null) { next = curr.next; curr.next = prev; prev = curr; curr = next; }
            head = prev;
            out.println("    [REVERSE] History order reversed.");
        }

        /**
         * CYCLE DETECTION -- Floyd's Tortoise & Hare -- O(n).
         * Detects corrupt pointer loops that would hang the Tracker screen.
         */
        boolean detectCycle() {
            SkillNode slow = head, fast = head;
            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
                if (slow == fast) {
                    out.println("    [CYCLE DETECT] Cycle FOUND -- pointer corruption!");
                    return true;
                }
            }
            out.println("    [CYCLE DETECT] No cycle -- list is healthy.");
            return false;
        }
    }


    // ===================================================================
    //  3. DOUBLY LINKED LIST -- Bidirectional Skill Catalogue
    //     Enables forward/backward paging through the skills directory
    //     (mirrors match-grid pagination concept in the HTML).
    // ===================================================================

    static class DNode {
        String skill, teacher;
        DNode prev, next;
        DNode(String skill, String teacher) { this.skill = skill; this.teacher = teacher; }
    }

    static class SkillCatalogue {
        DNode head, tail;

        /** Append at end -- O(1) */
        void append(String skill, String teacher) {
            DNode node = new DNode(skill, teacher);
            if (tail == null) { head = tail = node; return; }
            tail.next = node; node.prev = tail; tail = node;
        }

        /** Forward traversal -- O(n) */
        void traverseForward() {
            out.print("    Forward  -> ");
            DNode curr = head;
            while (curr != null) { out.print(curr.skill + (curr.next != null ? " -> " : "")); curr = curr.next; }
            out.println();
        }

        /** Backward traversal -- O(n)  (unique to Doubly LL) */
        void traverseBackward() {
            out.print("    Backward <- ");
            DNode curr = tail;
            while (curr != null) { out.print(curr.skill + (curr.prev != null ? " <- " : "")); curr = curr.prev; }
            out.println();
        }
    }


    // ===================================================================
    //  4. STACK -- Undo Request History  +  Expression Checker
    //     Mirrors the "undo last request" feature in the web app.
    // ===================================================================

    static Stack<Request> requestStack = new Stack<>();

    /** PUSH -- O(1). Push a new pending request onto the undo stack. */
    static void pushRequest(String from, String to, String skill) {
        Request r = new Request(from, to, skill, "pending");
        requestStack.push(r);
        out.println("    [PUSH] " + r);
    }

    /** POP -- O(1). Undo the last request sent. */
    static void popRequest() {
        if (!requestStack.isEmpty()) out.println("    [POP ] Undone: " + requestStack.pop());
        else out.println("    [POP ] Stack empty -- nothing to undo.");
    }

    /** PEEK -- O(1). View top without removing. */
    static void peekRequest() {
        if (!requestStack.isEmpty()) out.println("    [PEEK] Top: " + requestStack.peek());
    }

    /**
     * BALANCED PARENTHESES CHECK -- Stack application -- O(n).
     * Validates skill-tag syntax: "{Python, (DSA)}" entered in the
     * edit-profile modal of SkillSwap_Final.html.
     */
    static boolean checkBalanced(String expr) {
        Stack<Character> s = new Stack<>();
        for (char c : expr.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') s.push(c);
            else if (c == ')' || c == '}' || c == ']') {
                if (s.isEmpty()) return false;
                char top = s.pop();
                if ((c == ')' && top != '(') || (c == '}' && top != '{') || (c == ']' && top != '['))
                    return false;
            }
        }
        return s.isEmpty();
    }


    // ===================================================================
    //  5. QUEUE (FIFO) -- Request Processing Pipeline
    //     New exchange requests are queued and processed in arrival order,
    //     matching the requestQueue in the JS front-end.
    // ===================================================================

    static Queue<Request> requestQueue = new LinkedList<>();

    /** ENQUEUE -- O(1). Add incoming exchange request to the pipeline. */
    static void enqueueRequest(String from, String to, String skill) {
        Request r = new Request(from, to, skill, "pending");
        requestQueue.add(r);
        out.println("    [ENQUEUE] " + r);
    }

    /** DEQUEUE -- O(1). Pick up next request to process. */
    static void dequeueRequest() {
        if (!requestQueue.isEmpty()) {
            Request r = requestQueue.remove();
            r.status = "processing";
            out.println("    [DEQUEUE] Processing: " + r);
        } else {
            out.println("    [DEQUEUE] Queue empty.");
        }
    }


    // ===================================================================
    //  6. CIRCULAR QUEUE -- Round-Robin Session Scheduler
    //     Video call / study sessions are scheduled in circular order
    //     so no session starves while waiting.
    // ===================================================================

    static final int CQ_MAX = 6;
    static int[] circularQueue = new int[CQ_MAX];
    static int cqFront = -1, cqRear = -1, cqSize = 0;

    /** ENQUEUE circular -- O(1) */
    static void cqEnqueue(int sessionId) {
        if (cqSize == CQ_MAX) { out.println("    [CQ] Full -- cannot schedule " + sessionId); return; }
        if (cqFront == -1) cqFront = 0;
        cqRear = (cqRear + 1) % CQ_MAX;
        circularQueue[cqRear] = sessionId;
        cqSize++;
        out.printf("    [CQ ENQUEUE] Session %d  | front=%d rear=%d size=%d%n",
            sessionId, cqFront, cqRear, cqSize);
    }

    /** DEQUEUE circular -- O(1) */
    static void cqDequeue() {
        if (cqSize == 0) { out.println("    [CQ] Empty."); return; }
        out.printf("    [CQ DEQUEUE] Processing Session %d | remaining=%d%n",
            circularQueue[cqFront], cqSize - 1);
        cqFront = (cqFront + 1) % CQ_MAX;
        cqSize--;
        if (cqSize == 0) { cqFront = -1; cqRear = -1; }
    }


    // ===================================================================
    //  7. SEARCHING
    // ===================================================================

    /**
     * LINEAR SEARCH -- O(n)
     * Scans every student to find all teachers of a given skill.
     * Used in renderMatches() when no dept filter is applied.
     */
    static void linearSearch(List<Student> students, String skill) {
        out.println("    Linear Search for: \"" + skill + "\"  -- Time Complexity: O(n)");
        boolean found = false;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).teaches.stream().anyMatch(s -> s.equalsIgnoreCase(skill))) {
                out.println("      [FOUND] Index [" + i + "] : " + students.get(i).name
                    + " (dept: " + students.get(i).dept + ")");
                found = true;
            }
        }
        if (!found) out.println("      [NOT FOUND] No teacher found for \"" + skill + "\"");
    }

    /**
     * BINARY SEARCH -- O(log n)
     * Searches sorted-by-rep array for an exact reputation score.
     * Used in the leaderboard to locate a user's rank efficiently.
     * PRECONDITION: list must be sorted ascending by rep.
     */
    static int binarySearch(List<Student> sorted, int targetRep) {
        int low = 0, high = sorted.size() - 1, iter = 0;
        out.println("    Binary Search for rep=" + targetRep + "  -- Time Complexity: O(log n)");
        while (low <= high) {
            int mid = (low + high) / 2;
            iter++;
            out.printf("      Iter %d: low=%d mid=%d high=%d | mid.rep=%d%n",
                iter, low, mid, high, sorted.get(mid).rep);
            if (sorted.get(mid).rep == targetRep) {
                out.println("      [FOUND] " + sorted.get(mid).name + " at index " + mid);
                return mid;
            } else if (sorted.get(mid).rep < targetRep) low = mid + 1;
            else high = mid - 1;
        }
        out.println("      [NOT FOUND] rep=" + targetRep + " -- total iterations: " + iter);
        return -1;
    }


    // ===================================================================
    //  8. SORTING
    // ===================================================================

    /**
     * BUBBLE SORT -- O(n^2) time | O(1) space
     * Sorts leaderboard descending by reputation.
     * Early-exit optimisation: stops if no swaps in a full pass.
     */
    static void bubbleSort(List<Student> list) {
        int n = list.size(), swaps = 0, passes = 0;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            passes++;
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).rep < list.get(j + 1).rep) {
                    Collections.swap(list, j, j + 1);
                    swaps++; swapped = true;
                }
            }
            if (!swapped) break;
        }
        out.println("    Bubble Sort done  -- passes: " + passes + " | swaps: " + swaps + " | O(n^2)");
    }

    /**
     * SELECTION SORT -- O(n^2) time | O(1) space
     * Sorts students alphabetically by name.
     * Each pass finds the minimum and places it at the front.
     */
    static void selectionSort(List<Student> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++)
                if (list.get(j).name.compareToIgnoreCase(list.get(minIdx).name) < 0) minIdx = j;
            if (minIdx != i) Collections.swap(list, i, minIdx);
        }
        out.println("    Selection Sort done -- sorted alphabetically | O(n^2)");
    }

    /**
     * INSERTION SORT -- O(n^2) worst | O(n) best (nearly sorted) | O(1) space
     * Best case O(n) makes it efficient for incrementally adding students
     * to an already-sorted leaderboard (one new student joins at a time).
     */
    static void insertionSort(List<Student> list) {
        int n = list.size(), shifts = 0;
        for (int i = 1; i < n; i++) {
            Student key = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).rep < key.rep) {
                list.set(j + 1, list.get(j));
                j--; shifts++;
            }
            list.set(j + 1, key);
        }
        out.println("    Insertion Sort done -- shifts: " + shifts + " | O(n^2) worst, O(n) best");
    }

    /**
     * MERGE SORT -- O(n log n) time | O(n) space
     * Stable, efficient sort for the full campus leaderboard.
     * Stability preserves original order for students with equal rep.
     */
    static void mergeSort(List<Student> list, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(list, left, mid);
            mergeSort(list, mid + 1, right);
            merge(list, left, mid, right);
        }
    }

    static void merge(List<Student> list, int left, int mid, int right) {
        List<Student> L = new ArrayList<>(list.subList(left, mid + 1));
        List<Student> R = new ArrayList<>(list.subList(mid + 1, right + 1));
        int i = 0, j = 0, k = left;
        while (i < L.size() && j < R.size()) {
            if (L.get(i).rep >= R.get(j).rep) list.set(k++, L.get(i++));
            else list.set(k++, R.get(j++));
        }
        while (i < L.size()) list.set(k++, L.get(i++));
        while (j < R.size()) list.set(k++, R.get(j++));
    }

    /**
     * QUICK SORT -- O(n log n) avg | O(n^2) worst | O(log n) space
     * Ranks match cards by compatibility score before rendering the
     * match grid. Mirrors JS: .sort((a,b) => b.score - a.score)
     * Pivot = last element.
     */
    static void quickSort(List<Student> list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quickSort(list, low, pi - 1);
            quickSort(list, pi + 1, high);
        }
    }

    static int partition(List<Student> list, int low, int high) {
        int pivot = list.get(high).rep;
        int i = low - 1;
        for (int j = low; j < high; j++)
            if (list.get(j).rep >= pivot) { i++; Collections.swap(list, i, j); }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }


    // ===================================================================
    //  9. HASHING -- Skill Directory  (Separate Chaining)
    //     Provides O(1) average lookup for "who teaches X?".
    //     Mirrors the Firebase real-time skill lookup in renderMatches().
    //     TABLE_SIZE = 13 (prime) to reduce clustering.
    // ===================================================================

    static final int TABLE_SIZE = 13;
    @SuppressWarnings("unchecked")
    static List<String[]>[] hashTable = new ArrayList[TABLE_SIZE];

    /**
     * Hash Function -- polynomial rolling hash
     * h(key) = sum(char * 31^i) mod TABLE_SIZE
     * Multiplier 31 is prime, spreads common skill names across buckets.
     */
    static int hashFunction(String key) {
        int hash = 0;
        for (char c : key.toLowerCase().toCharArray())
            hash = (hash * 31 + c) % TABLE_SIZE;
        return Math.abs(hash);
    }

    /** HASH INSERT -- O(1) avg. Adds (skill -> teacher) with separate chaining. */
    static void hashInsert(String skill, String teacher) {
        int idx = hashFunction(skill);
        if (hashTable[idx] == null) hashTable[idx] = new ArrayList<>();
        hashTable[idx].add(new String[]{skill, teacher});
        out.printf("    [HASH INSERT] %-22s -> bucket[%2d] | teacher: %s%n",
            "\"" + skill + "\"", idx, teacher);
    }

    /** HASH SEARCH -- O(1) avg. Returns all teachers for a skill. */
    static void hashSearch(String skill) {
        int idx = hashFunction(skill);
        out.printf("    [HASH SEARCH] %-22s -> bucket[%2d]  ", "\"" + skill + "\"", idx);
        if (hashTable[idx] != null) {
            List<String> found = new ArrayList<>();
            for (String[] e : hashTable[idx])
                if (e[0].equalsIgnoreCase(skill)) found.add(e[1]);
            if (!found.isEmpty()) { out.println("OK  Teachers: " + found); return; }
        }
        out.println("NOT FOUND");
    }

    /** HASH DELETE -- O(1) avg. Removes a skill (e.g. profile update). */
    static void hashDelete(String skill) {
        int idx = hashFunction(skill);
        if (hashTable[idx] != null) {
            boolean removed = hashTable[idx].removeIf(e -> e[0].equalsIgnoreCase(skill));
            out.printf("    [HASH DELETE] %-22s -> bucket[%2d]  %s%n",
                "\"" + skill + "\"", idx, removed ? "Removed." : "Not found.");
        } else {
            out.println("    [HASH DELETE] \"" + skill + "\" -- bucket empty.");
        }
    }


    // ===================================================================
    // 10. MATCHING ALGORITHM
    //     Direct Java equivalent of renderMatches() in HTML/JS:
    //
    //       score += 2  if other.teaches contains something I want
    //       score += 2  if other.wants   contains something I teach
    //
    //     Students sorted by descending matchScore.
    //     Labels: >=6 "Perfect Match" | >=3 "Great Match" | >0 "Good Match"
    // ===================================================================

    static void runMatchingAlgorithm(Student me, List<Student> others) {
        out.println("  Computing match scores for: " + me.name);
        out.println("  (Mirrors renderMatches() scoring in SkillSwap_Final.html)");
        out.println();

        List<String> myWants   = new ArrayList<>();
        List<String> myTeaches = new ArrayList<>();
        for (String s : me.wants)   myWants.add(s.toLowerCase());
        for (String s : me.teaches) myTeaches.add(s.toLowerCase());

        for (Student s : others) {
            int score = 0;
            for (String t : s.teaches)
                for (String w : myWants)
                    if (t.toLowerCase().contains(w) || w.contains(t.toLowerCase())) { score += 2; break; }
            for (String w : s.wants)
                for (String t : myTeaches)
                    if (w.toLowerCase().contains(t) || t.contains(w.toLowerCase())) { score += 2; break; }
            s.matchScore = score;
        }

        others.sort((a, b) -> b.matchScore - a.matchScore);

        out.printf("  %-18s | %-4s | Score | Match Label%n", "Name", "Dept");
        out.println("  " + "-".repeat(58));
        for (Student s : others) {
            String label = s.matchScore >= 6 ? "*** Perfect Match"
                         : s.matchScore >= 3 ? "**  Great Match"
                         : s.matchScore >  0 ? "*   Good Match"
                         :                     "    No Match";
            out.printf("  %-18s | %-4s |  %3d  | %s%n",
                s.name, s.dept, s.matchScore, label);
        }
    }


    // ===================================================================
    //  HELPERS
    // ===================================================================

    static void banner(String text) {
        String line = "=".repeat(66);
        out.println();
        out.println("  " + line);
        out.println("  " + text);
        out.println("  " + line);
    }

    static void section(int num, String title) {
        out.println();
        out.println("  ------------------------------------------------------------------");
        out.printf("  %d. %s%n", num, title);
        out.println("  ------------------------------------------------------------------");
    }

    static void sub(String title) { out.println("\n  >> " + title); }

    static void printLeaderboard(List<Student> sorted, String method) {
        out.println("  " + method + " Leaderboard:");
        String[] medals = {"[GOLD]  ", "[SILVER]", "[BRONZE]"};
        for (int i = 0; i < sorted.size(); i++) {
            String rank = i < 3 ? medals[i] : "  [" + (i + 1) + "]   ";
            out.printf("    %s %-18s -- %d pts%n", rank, sorted.get(i).name, sorted.get(i).rep);
        }
    }


    // ===================================================================
    //  MAIN
    // ===================================================================

    public static void main(String[] args) throws Exception {

        // Optional: tee output to a file as well
        if (SAVE_TO_FILE) {
            PrintStream fileOut = new PrintStream(new FileOutputStream("SkillSwap_Output.txt"));
            out = new PrintStream(new TeeOutputStream(System.out, fileOut));
        }

        // Force UTF-8 on Windows consoles (safe no-op on Linux/Mac)
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            out = System.out;
        } catch (Exception ignored) {}

        out.println();
        out.println("  ================================================================");
        out.println("       SKILLSWAP -- DSA FULL DEMONSTRATION");
        out.println("       Campus Skill Exchange Network -- KL University");
        out.println("       Student : T. Sai Sri Parinita  |  Roll No : 2520030562");
        out.println("  ================================================================");

        // ---------------------------------------------------------------
        //  SAMPLE DATA -- mirrors Firestore documents in the HTML project
        // ---------------------------------------------------------------
        List<Student> students = new ArrayList<>(Arrays.asList(
            new Student(1, "Arjun Sharma",   "CSE",  Arrays.asList("Python","DSA"),               Arrays.asList("UI Design","Figma"),        120),
            new Student(2, "Priya Nair",     "ECE",  Arrays.asList("UI Design","Figma"),           Arrays.asList("Python","ML"),               95),
            new Student(3, "Rohan Mehta",    "CSE",  Arrays.asList("React","JavaScript"),          Arrays.asList("Machine Learning","DSA"),    150),
            new Student(4, "Sneha Patel",    "IT",   Arrays.asList("Machine Learning","Python"),   Arrays.asList("React","JavaScript"),         80),
            new Student(5, "Karan Iyer",     "EEE",  Arrays.asList("Arduino","IoT","C++"),         Arrays.asList("DSA","Python"),               60),
            new Student(6, "Divya Krishnan", "CSE",  Arrays.asList("DSA","C++","Algorithms"),      Arrays.asList("Figma","UI Design"),         110),
            new Student(7, "Meera Reddy",    "IT",   Arrays.asList("Data Analysis","SQL"),         Arrays.asList("React","JavaScript"),         75),
            new Student(8, "Aditya Rao",     "CSE",  Arrays.asList("Flutter","Dart"),              Arrays.asList("DSA","Python"),               40)
        ));


        // ===============================================================
        section(1, "ARRAYS / ARRAYLISTS -- Student Profile Store");
        // ===============================================================
        out.println("  Total registered students: " + students.size());
        out.println();
        out.printf("  %-5s %-18s %-5s %-32s %-26s %s%n",
            "ID", "Name", "Dept", "Teaches", "Wants", "Rep");
        out.println("  " + "-".repeat(95));
        students.forEach(out::println);

        sub("Access / Update / Add  -- O(1) avg");
        Student newStudent = new Student(9, "Pooja Singh", "BBA",
            Arrays.asList("MS Office","Excel"), Arrays.asList("Python"), 10);
        students.add(newStudent);
        out.println("    [ADD]    " + newStudent.name + " added. Total: " + students.size());
        out.println("    [GET]    Student at index 2 -> " + students.get(2).name);
        students.get(0).rep += 30;
        out.println("    [UPDATE] " + students.get(0).name + " rep updated -> " + students.get(0).rep);
        students.remove(students.size() - 1);
        out.println("    [REMOVE] Demo entry removed. Total: " + students.size());


        // ===============================================================
        section(2, "SINGLY LINKED LIST -- Skill Exchange History");
        // ===============================================================
        SkillExchangeHistory history = new SkillExchangeHistory();

        sub("Insert O(1)");
        history.insert("Python",           "Priya Nair");
        history.insert("UI Design",        "Arjun Sharma");
        history.insert("React",            "Sneha Patel");
        history.insert("DSA",              "Karan Iyer");
        history.insert("Machine Learning", "Rohan Mehta");

        sub("Traverse O(n)");
        history.traverse();

        sub("Search O(n)");
        history.search("React");
        history.search("Blockchain");

        sub("Delete O(n)");
        history.delete("UI Design");
        history.delete("Figma");
        history.traverse();

        sub("Reverse O(n)  [Extra Feature -- +1 mark]");
        history.reverse();
        history.traverse();

        sub("Cycle Detection -- Floyd's Tortoise & Hare O(n)  [Extra Feature]");
        history.detectCycle();
        // Manually introduce a cycle for demonstration
        SkillNode tail = history.head;
        while (tail.next != null) tail = tail.next;
        tail.next = history.head.next;          // create cycle
        out.print("    After introducing cycle: ");
        history.detectCycle();
        tail.next = null;                        // remove cycle
        out.print("    After removing  cycle: ");
        history.detectCycle();


        // ===============================================================
        section(3, "DOUBLY LINKED LIST -- Bidirectional Skill Catalogue  [Extra Feature]");
        // ===============================================================
        SkillCatalogue catalogue = new SkillCatalogue();
        out.println("    Building catalogue from student profiles...");
        students.forEach(s -> catalogue.append(s.teaches.get(0), s.name));

        sub("Forward traversal -- O(n)");
        catalogue.traverseForward();

        sub("Backward traversal -- O(n)  (unique to Doubly LL)");
        catalogue.traverseBackward();


        // ===============================================================
        section(4, "STACK -- Undo Request History  +  Expression Checker");
        // ===============================================================
        sub("Push / Pop / Peek -- O(1)");
        pushRequest("Arjun",  "Priya",   "UI Design");
        pushRequest("Sneha",  "Rohan",   "React");
        pushRequest("Karan",  "Arjun",   "DSA");
        pushRequest("Divya",  "Priya",   "Figma");
        peekRequest();
        popRequest();
        popRequest();
        out.println("    Stack size after 2 undos: " + requestStack.size());

        sub("Balanced Parentheses / Expression Check -- O(n)  [Stack Application]");
        String[] exprs = {
            "(Python + {DSA})",
            "(React + [UI Design)",
            "{{Figma} + (C++)}",
            "[Arduino + {IoT}]"
        };
        for (String e : exprs)
            out.println("    " + e + "  ->  " + (checkBalanced(e) ? "Balanced" : "NOT Balanced"));


        // ===============================================================
        section(5, "QUEUE (FIFO) -- Request Processing Pipeline");
        // ===============================================================
        sub("Enqueue -- O(1)");
        enqueueRequest("Divya",  "Priya",  "Figma");
        enqueueRequest("Priya",  "Arjun",  "Python");
        enqueueRequest("Karan",  "Divya",  "C++");
        enqueueRequest("Meera",  "Rohan",  "React");
        out.println("    Queue size: " + requestQueue.size());

        sub("Dequeue -- O(1)");
        dequeueRequest();
        dequeueRequest();
        out.println("    Remaining in queue: " + requestQueue.size());

        sub("Circular Queue -- Round-Robin Session Scheduler -- O(1)  [CQ Application]");
        out.println("    CQ capacity = " + CQ_MAX);
        cqEnqueue(101); cqEnqueue(102); cqEnqueue(103); cqEnqueue(104);
        cqDequeue(); cqDequeue();
        cqEnqueue(105); cqEnqueue(106);
        cqDequeue();
        cqEnqueue(107);
        out.println("    Current CQ size: " + cqSize);


        // ===============================================================
        section(6, "SEARCHING");
        // ===============================================================
        sub("Linear Search O(n) -- find all teachers of a skill");
        linearSearch(students, "DSA");
        out.println();
        linearSearch(students, "Python");
        out.println();
        linearSearch(students, "Blockchain");

        sub("Binary Search O(log n) -- find student by exact rep score");
        List<Student> sortedAsc = new ArrayList<>(students);
        sortedAsc.sort(Comparator.comparingInt(s -> s.rep));
        out.println("    Array sorted ascending by rep:");
        sortedAsc.forEach(s -> out.printf("      %-18s rep=%d%n", s.name, s.rep));
        out.println();
        binarySearch(sortedAsc, 110);
        out.println();
        binarySearch(sortedAsc, 999);


        // ===============================================================
        section(7, "SORTING");
        // ===============================================================

        sub("(i) Bubble Sort -- O(n^2) | Leaderboard descending by rep");
        List<Student> s1 = new ArrayList<>(students);
        bubbleSort(s1);
        printLeaderboard(s1, "Bubble Sort");

        sub("(ii) Selection Sort -- O(n^2) | Alphabetical by name");
        List<Student> s2 = new ArrayList<>(students);
        selectionSort(s2);
        out.println("  Selection Sort Alphabetical:");
        for (int i = 0; i < s2.size(); i++)
            out.printf("    %d. %s%n", i + 1, s2.get(i).name);

        sub("(iii) Insertion Sort -- O(n^2) worst / O(n) best | Incremental leaderboard");
        List<Student> s3 = new ArrayList<>(students);
        insertionSort(s3);
        printLeaderboard(s3, "Insertion Sort");

        sub("(iv) Merge Sort -- O(n log n) | Full campus leaderboard (stable)");
        List<Student> s4 = new ArrayList<>(students);
        mergeSort(s4, 0, s4.size() - 1);
        out.println("    Merge Sort done -- O(n log n)");
        printLeaderboard(s4, "Merge Sort");

        sub("(v) Quick Sort -- O(n log n) avg | Match ranking by score");
        List<Student> s5 = new ArrayList<>(students);
        quickSort(s5, 0, s5.size() - 1);
        out.println("    Quick Sort done -- O(n log n) avg, O(n^2) worst");
        printLeaderboard(s5, "Quick Sort");


        // ===============================================================
        section(8, "HASHING -- Skill Directory (Separate Chaining)");
        // ===============================================================
        Arrays.fill(hashTable, null);

        sub("Hash Insert -- O(1) avg");
        students.forEach(s -> s.teaches.forEach(skill -> hashInsert(skill, s.name)));

        sub("Hash Search -- O(1) avg");
        out.println();
        hashSearch("Python");
        hashSearch("DSA");
        hashSearch("React");
        hashSearch("Blockchain");

        sub("Hash Delete -- O(1) avg");
        hashDelete("Figma");
        hashSearch("Figma");

        sub("Hash Table bucket distribution");
        out.println("    Bucket | Entries");
        out.println("    " + "-".repeat(50));
        for (int i = 0; i < TABLE_SIZE; i++) {
            if (hashTable[i] != null && !hashTable[i].isEmpty()) {
                out.printf("    [%2d]   | ", i);
                hashTable[i].forEach(e -> out.print(e[0] + "  "));
                out.println();
            }
        }


        // ===============================================================
        section(9, "SKILLSWAP MATCHING ALGORITHM -- mirrors JS renderMatches()");
        // ===============================================================
        Student loggedIn = students.get(0);   // Arjun Sharma is logged in
        List<Student> others = new ArrayList<>(students.subList(1, students.size()));
        runMatchingAlgorithm(loggedIn, others);


        // ===============================================================
        out.println();
        out.println("  ================================================================");
        out.println("    ALL DSA CONCEPTS DEMONSTRATED SUCCESSFULLY!");
        out.println();
        out.println("    Arrays / Singly LL / Doubly LL / Cycle Detection");
        out.println("    Stack / Queue / Circular Queue");
        out.println("    Linear Search / Binary Search");
        out.println("    Bubble / Selection / Insertion / Merge / Quick Sort");
        out.println("    Hashing (Separate Chaining) / Matching Algorithm");
        out.println();
        out.println("    SkillSwap -- Campus Skill Exchange Network");
        out.println("    Student: T. Sai Sri Parinita  |  Roll: 2520030562");
        out.println("  ================================================================");
        out.println();
    }


    // ===================================================================
    //  TeeOutputStream -- writes to two streams simultaneously
    //  Used only when SAVE_TO_FILE = true
    // ===================================================================
    static class TeeOutputStream extends OutputStream {
        private final OutputStream a, b;
        TeeOutputStream(OutputStream a, OutputStream b) { this.a = a; this.b = b; }
        @Override public void write(int x)             throws IOException { a.write(x); b.write(x); }
        @Override public void write(byte[] d, int o, int l) throws IOException { a.write(d,o,l); b.write(d,o,l); }
        @Override public void flush()                  throws IOException { a.flush(); b.flush(); }
    }
}
