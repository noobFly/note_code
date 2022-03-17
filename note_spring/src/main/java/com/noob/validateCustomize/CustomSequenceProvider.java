package com.noob.validateCustomize;

import com.google.common.collect.Lists;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.List;

public class CustomSequenceProvider implements DefaultGroupSequenceProvider<CustomGroupForm> {

    @Override
    public List<Class<?>> getValidationGroups(CustomGroupForm form) {
        List<Class<?>> defaultGroupSequence = Lists.newArrayList();

        defaultGroupSequence.add(CustomGroupForm.class); // 这一步不能省,否则Default分组都不会执行了

        if (form != null && "A".equals(form.getType())) {
            defaultGroupSequence.add(CustomGroupForm.WhenTypeIsA.class);
        }

        if (form != null && "B".equals(form.getType())) {
            defaultGroupSequence.add(CustomGroupForm.WhenTypeIsB.class);
        }
        return defaultGroupSequence;
    }
}