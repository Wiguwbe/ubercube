#version 330 core
#extension GL_NV_shadow_samplers_cube : enable
#define FOG_COLOR vec4(221.0 / 255.0, 232.0 / 255.0, 255.0 / 255.0, 1.0)
#define SHADOW_CASCADE_COUNT 3

out vec4 fragColor;
uniform sampler2D shadowMap[SHADOW_CASCADE_COUNT];
uniform vec3 cameraPosition;
uniform float fogDistance;
uniform vec4 in_color;
uniform float shadowCascadeDistances[SHADOW_CASCADE_COUNT + 1];

in vec3 v_color;
in vec3 v_normal;
in vec3 worldPosition;
in vec4 lightPositions[SHADOW_CASCADE_COUNT];
in float shadowDist;
in float zDist;
in float renderShadows;

void main(void)
{
	float fragDist = distance(cameraPosition.xz, worldPosition.xz);
	float dist = fragDist / fogDistance * 2 - 0.8;
	if (dist > 1)
		dist = 1;
	if (dist < 0)
		dist = 0;

	vec4 color = vec4(v_color, 1.0);
	if (in_color != vec4(-1, -1, -1, -1))
		color = in_color;

	vec4 shadow = vec4(1, 1, 1, 1);
	if (renderShadows > 5)
	{
		float shadowFactor = 1.0;
		int shadowMapID = SHADOW_CASCADE_COUNT - 1;
		for (int i = 0; i< SHADOW_CASCADE_COUNT; i++)
		{
			if (zDist < shadowCascadeDistances[i + 1])
			{
				shadowMapID = i;
				break;
			}
		}
		vec4 lightPosition = lightPositions[shadowMapID];

		vec3 projCoords = lightPosition.xyz / lightPosition.w;
		projCoords = projCoords * 0.5 + 0.5;

		float closestDepth;
		switch(shadowMapID) {
			case 0:
				closestDepth = texture(shadowMap[0], projCoords.xy).r;
				break;
			case 1:
				closestDepth = texture(shadowMap[1], projCoords.xy).r;
				break;
			default:
				closestDepth = texture(shadowMap[2], projCoords.xy).r;
		}
		//1float closestDepth = texture(shadowMap[shadowMapID], projCoords.xy).r;
		float currentDepth = projCoords.z;

		float bias = 0.00001 * (10.0 * (shadowMapID + 1));

		if (currentDepth - bias > closestDepth)
			shadowFactor = 1.0 - 0.5;
		shadow = vec4(shadowFactor, shadowFactor, shadowFactor, 1.0);
	}
	vec4 finalColor = color * 1.2f * shadow;
	fragColor = mix(finalColor, FOG_COLOR, dist);
}
