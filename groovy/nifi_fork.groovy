class GroovyProcessor implements Processor {

    def REL_ORIGINAL = new Relationship.Builder().name("original").description("Original flow").build();
    def REL_FORK     = new Relationship.Builder().name("fork").description("Forked flow").build();

    @Override
    void initialize(ProcessorInitializationContext context) {
    }

    @Override
    Set<Relationship> getRelationships() {
        return [REL_ORIGINAL, REL_FORK] as Set
    }

    @Override
    void onTrigger(ProcessContext context, ProcessSessionFactory sessionFactory) throws ProcessException {
        try {

            def session = sessionFactory.createSession()
            def flowFile = session.get()
            if (!flowFile) return

            def clone = session.clone(flowFile)

            // Set the parent
            clone = session.putAttribute(clone,"parent", flowFile.getAttribute("uuid"))
          
            // Set the child
            flowFile = session.putAttribute(flowFile,"child", clone.getAttribute("uuid"))

            // transfer
            session.transfer(flowFile, REL_ORIGINAL)
            session.transfer(clone, REL_FORK)
            session.commit()
        }
        catch (e) {
            throw new ProcessException(e)
        }
    }

    @Override
    Collection<ValidationResult> validate(ValidationContext context) { return null }

    @Override
    PropertyDescriptor getPropertyDescriptor(String name) { return null }

    @Override

    void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) { }

    @Override

    List<PropertyDescriptor> getPropertyDescriptors() { return null }

    @Override

    String getIdentifier() { return null }
}

processor = new GroovyProcessor()
