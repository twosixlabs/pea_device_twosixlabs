# Build an addon src jar file out of the generated stubs
# Input variable:
#   addon_stub_name: the name of the addon stubs; the stub source code should have been generated to
#                  $(TARGET_OUT_COMMON_INTERMEDIATES)/JAVA_LIBRARIES/$(addon_stub_name)_intermediates.
#   stub_timestamp: the timestamp file we use as dependency of the generated source.
# Output variable:
#   full_src_target: the built classes.jar

$(warning build_addon_stubs, stub_name $(addon_stub_name) timestamp $(stub_timestamp))

intermediates := out/target/common/obj/JAVA_LIBRARIES/$(addon_stub_name)_intermediates
full_src_target = $(intermediates)/android-stubs-src.jar
src_dir := $(intermediates)/src
classes_dir := $(intermediates)/classes

$(full_src_target): PRIVATE_SRC_DIR := $(src_dir)
$(full_src_target): PRIVATE_INTERMEDIATES_DIR := $(intermediates)

$(full_src_target): $(stub_timestamp)
	@echo Packaging Addon Stub sources: $@
	cd $(PRIVATE_INTERMEDIATES_DIR) && zip -rq $(notdir $@) $(notdir $(PRIVATE_SRC_DIR))
