package gr.spinellis.ckjm;

public class DummyClass {

    // WMC (Weighted Methods per Class)
    public void method1() { /* simple method */ }
    public void method2() { /* simple method */ }
    public void method3() { /* simple method */ }
    public void method4() { /* simple method */ }

    // DIT (Depth of Inheritance Tree)
    public static class BaseClass {}
    public static class DerivedClass extends BaseClass {}
    public static class MoreDerivedClass extends DerivedClass {}

    // CBO (Coupling Between Object classes)
    public class CoupledClass {
        private OtherClass otherClass;
        public CoupledClass() {
            this.otherClass = new OtherClass();
        }
        public void doSomething() {
            otherClass.someMethod();
        }
    }
    public class OtherClass {
        public void someMethod() { /* some implementation */ }
    }

    // RFC (Response For a Class)
    public class ResponseClass {
        public void responseMethod1() { /* response method */ }
        public void responseMethod2() { /* response method */ }
        public void responseMethod3() { /* response method */ }
    }

    // LCOM (Lack of Cohesion of Methods)
    private int field1;
    private int field2;
    private int field3;
    public void setField1(int value) { this.field1 = value; }
    public void setField2(int value) { this.field2 = value; }
    public void setField3(int value) { this.field3 = value; }
    public int getField1() { return field1; }
    public int getField2() { return field2; }
    public int getField3() { return field3; }

    // CA (Afferent Couplings) and CE (Efferent Couplings)
    // This requires more context as it depends on the number of classes using and being used by this class.
    // For simplicity, let's define this in a simplified way:
    
    // Example classes that DummyClass depends on (Efferent Couplings)
    public class DependencyClass1 {}
    public class DependencyClass2 {}

    public static void main(String[] args) {
        DummyClass demo = new DummyClass();
        demo.method1();
        demo.method2();
        demo.method3();
        demo.method4();
    }
}